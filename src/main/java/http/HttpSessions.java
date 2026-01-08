package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpSessions {
    private static final Logger logger = LoggerFactory.getLogger(HttpSessions.class);

    // [구조 변경]
    // 1. UserID로 세션 객체를 관리 (1인 1세션 보장용)
    private static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    // 2. 랜덤 SID(UUID)로 UserID를 찾기 위한 역방향 맵 (로그인 유지 확인용)
    private static Map<String, String> idToUser = new ConcurrentHashMap<>();

    private static final long SESSION_TIMEOUT = 3 * 60 * 1000; // 3분

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    static {
        // 백그라운드 스케줄러 시작 (1분 주기)
        scheduler.scheduleAtFixedRate(HttpSessions::clearInvalidSessions, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * DispatcherServlet에서 쿠키에 담긴 랜덤 SID(UUID)로 세션을 찾을 때 사용합니다.
     */
    public static HttpSession getSession(String sid) {
        String userId = idToUser.get(sid);
        if (userId == null) return null;
        return sessions.get(userId);
    }

    /**
     * 로그인 시 호출됩니다.
     * UserID를 기준으로 기존 세션이 있으면 재사용하고, 없으면 랜덤 SID를 가진 새 세션을 만듭니다.
     */
    public static HttpSession getOrCreateSession(String userId) {
        // 이미 해당 유저의 세션이 존재하면 반환
        if (sessions.containsKey(userId)) {
            HttpSession session = sessions.get(userId);
            session.access(); // 접근 시간 갱신
            return session;
        }

        // 없다면 보안을 위해 랜덤 SID(UUID) 생성
        String randomSid = UUID.randomUUID().toString();
        HttpSession session = new HttpSessionImpl(randomSid);

        // 두 저장소에 각각 저장
        sessions.put(userId, session);      // UserID -> Session (중복 방지용)
        idToUser.put(randomSid, userId);    // SID -> UserID (조회용)

        logger.info("🔑 새 세션 발급 완료: UserID = {}, SID(UUID) = {}", userId, randomSid);
        return session;
    }

    public static void remove(String sid) {
        String userId = idToUser.remove(sid);
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    /**
     * 스케줄러에 의해 1분마다 호출되는 청소 로직
     */
    public static void clearInvalidSessions() {
        if (sessions.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        // 1. 만료된 세션 삭제 (UserID 맵 기준)
        sessions.entrySet().removeIf(entry -> {
            HttpSession session = entry.getValue();
            long idleTime = currentTime - session.getLastAccessedTime();

            if (idleTime > SESSION_TIMEOUT) {
                logger.info(" [세션 만료 삭제] UserID: {} | SID: {} ({}초 경과)",
                        entry.getKey(), session.getId(), idleTime / 1000);

                // 역방향 맵에서도 같이 지워줘야 함
                idToUser.remove(session.getId());
                return true;
            }
            return false;
        });

        // 2. 생존 현황 로그 (디버깅용)
        int activeCount = sessions.size();
        if (activeCount > 0) {
            logger.debug(" [세션 생존 현황] 총 {}개 활성 중", activeCount);
            sessions.forEach((userId, session) -> {
                long idleTimeSec = (currentTime - session.getLastAccessedTime()) / 1000;
                long remainingTimeSec = (SESSION_TIMEOUT / 1000) - idleTimeSec;
                logger.debug("   [생존] ID: {} (SID: {}) | 만료까지: {}초 남음",
                        userId, session.getId(), remainingTimeSec);
            });
            logger.debug("--------------------------------------------------");
        }
    }
}