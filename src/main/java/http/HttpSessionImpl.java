package http;

import java.util.HashMap;
import java.util.Map;

public class HttpSessionImpl implements  HttpSession {
        private  String id;
        private long lastAccessedTime;
        private Map<String, Object> attributes = new HashMap<>();

        public HttpSessionImpl(String id){
            this.id = id;
            this.lastAccessedTime = System.currentTimeMillis();
        }

    @Override
    public String getId() {
        return "id";
    }


    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void invalidate() {
        attributes.clear();
    }

    @Override
    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }
}
