package http;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private String method;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public RequestLine(String requestLine) {
        // 예: "GET /user/create?userId=test&password=123 HTTP/1.1"
        if (requestLine == null || requestLine.isEmpty()) return;

        String[] tokens = requestLine.split(" ");
        if (tokens.length < 2) return;

        this.method = tokens[0];
        String url = tokens[1];

        if (url.contains("?")) {
            int index = url.indexOf("?");
            this.path = url.substring(0, index);
            String queryString = url.substring(index + 1);
            parseQueryString(queryString);
        } else {
            this.path = url;
        }
    }

    private void parseQueryString(String queryString) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length >= 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getParams() { return params; }
}