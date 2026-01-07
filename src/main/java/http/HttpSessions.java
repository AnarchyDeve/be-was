package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessions {
    // 1. 로거 선언 (클래스 레벨)
    private static final Logger logger = LoggerFactory.getLogger(HttpSessions.class);

    private static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private static final int TIMEOUT = 30;

    public static HttpSession getSession(String id) {
        return sessions.get(id);
    }

    public static void addSession(HttpSession session) {
        sessions.put(session.getId(), session);
        logger.info("새 세션 등록 완료: ID = {}", session.getId());
    }

    public static void remove(String id) {
        sessions.remove(id);
    }

    public static void clearInvalidSessions() {
        int beforeSize = sessions.size();

        // [중요] 세션이 0개여도 호출은 되었다는 로그를 남겨야 32라인 블래킹 여부를 압니다.
        if (beforeSize == 0) {
            logger.debug("세션 청소기 호출됨: 현재 활성화된 세션 없음.");
            return;
        }

        logger.info("========== 세션 청소기 작동 (전체 세션: {}) ==========", beforeSize);
        long currentTime = System.currentTimeMillis();

        // 실제 삭제 로직
        sessions.entrySet().removeIf(entry -> {
            HttpSessionImpl session = (HttpSessionImpl) entry.getValue();
            long idleTime = (currentTime - session.getLastAccessedTime()) / 1000;

            logger.info("-> ID: {} | 경과: {}초", session.getId(), idleTime);

            if (idleTime > TIMEOUT) {
                logger.warn("   [!] 만료됨: 세션을 삭제합니다. (ID: {})", session.getId());
                return true;
            }
            return false;
        });

        int afterSize = sessions.size();
        logger.info("[청소 후] 남은 세션 수: {}", afterSize);
        logger.info("================================================");
    }
}