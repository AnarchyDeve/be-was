package http;

public interface HttpSession {
    String getId();
    void setAttribute(String name, Object value);
    Object getAttribute(String name);
    void removeAttribute(String name);
    void invalidate();
    long getLastAccessedTime();
    void access(); // 마지막 접근 시간 갱신
}