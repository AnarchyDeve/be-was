package http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private RequestLine requestLine;   // [부품 1]
    private HttpCookies httpCookies;   // [부품 2]

    private Map<String, String> params = new HashMap<>();
    private HttpSession session;       // 연결된 세션

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            if (line == null) return;

            // 1. 첫 줄 처리는 RequestLine에게 위임
            this.requestLine = new RequestLine(line);
            this.params.putAll(requestLine.getParams()); // GET 파라미터 가져오기

            // 2. 헤더 처리
            int contentLength = 0;
            while ((line = br.readLine()) != null && !line.equals("")) {
                String lowerLine = line.toLowerCase();

                if (lowerLine.startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }

                // 쿠키 처리는 HttpCookies에게 위임
                if (lowerLine.startsWith("cookie:")) {
                    this.httpCookies = new HttpCookies(line.split(":")[1].trim());
                }
            }

            // 3. POST Body 처리
            if ("POST".equals(getMethod()) && contentLength > 0) {
                char[] body = new char[contentLength];
                br.read(body, 0, contentLength);
                parseBodyParameters(new String(body));
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void parseBodyParameters(String bodyData) {
        String[] pairs = bodyData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length >= 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
    }

    // --- Getter & Setter ---

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getCookie(String name) {
        if (httpCookies == null) return null;
        return httpCookies.getCookie(name);
    }

    public String getParameter(String name) { return params.get(name); }
    public void setSession(HttpSession session) { this.session = session; }
    public HttpSession getSession() { return session; }
}