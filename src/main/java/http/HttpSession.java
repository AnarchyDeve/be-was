package http;

public interface HttpSession {
    String getId() ;                    // 세션의 고유 ID(열쇠)를 가져옴
    void setAttribute(String name, Object value); // 세션에 데이터 보관 (유저 객체 등)
    Object getAttribute(String name);  // 보관된 데이터 꺼내기
    void removeAttribute(String name); // 특정 데이터 삭제
    void invalidate();                 // 세션 전체 무효화 (로그아웃용)
    void access();
    long getLastAccessedTime();
}
