package http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HttpSessions {
    private static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private static final int TIMEOUT = 30;

    public static HttpSession getSession(String id) {
        return sessions.get(id);
    }

    public static void addSession(HttpSession session) {
        sessions.put(session.getId(), session);
    }

    public static void remove(String id) {
        sessions.remove(id);
    }

    public static void clearInvalidSessions() {
        // 1. 청소 시작 전 상태 확인
        int beforeSize = sessions.size();
        if (beforeSize == 0) return; // 청소할 게 없으면 그냥 나감 (로그 공해 방지)

        System.out.println("\n========== 세션 청소기 작동 ==========");
        System.out.println("[청소 전] 현재 보관 중인 세션 수: " + beforeSize);

        long currentTime = System.currentTimeMillis();

        // 2. 실제 삭제 로직 (30초 기준)
        sessions.entrySet().removeIf(entry -> {
            HttpSessionImpl session = (HttpSessionImpl) entry.getValue();
            long idleTime = (currentTime - session.getLastAccessedTime()) / 1000;

            System.out.println("-> ID: " + session.getId() + " | 경과: " + idleTime + "초");

            if (idleTime > 30) {
                System.out.println("   [!] 만료됨: 이 세션을 삭제합니다.");
                return true;
            }
            return false;
        });

        // 3. 청소 후 상태 확인
        int afterSize = sessions.size();
        System.out.println("[청소 후] 남은 세션 수: " + afterSize);
        System.out.println("====================================\n");
    }
}