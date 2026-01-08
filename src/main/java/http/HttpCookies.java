package http;

import java.util.HashMap;
import java.util.Map;

public class HttpCookies {
    private Map<String, String> cookies = new HashMap<>();

    public HttpCookies(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isEmpty()) return;
        parse(cookieHeader);
    }

    private void parse(String cookieHeader) {
        // 예: "sid=javajigi; log=true"
        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                cookies.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }
}