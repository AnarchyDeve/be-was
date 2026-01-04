package http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine(); // Request Line: "GET /user/create?userId=... HTTP/1.1"

            if (line == null) return;

            // 1. 리퀘스트 라인 분리
            String[] tokens = line.split(" ");
            this.method = tokens[0];
            String url = tokens[1]; // "/user/create?userId=aaaaa&name=11111..."

            // 2. 경로(path)와 파라미터(params) 분리 처리
            parseUrl(url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseUrl(String url) {
        if (url.contains("?")) {
            // ?를 기준으로 쪼갬 (메타문자이므로 \\? 사용)
            String[] parts = url.split("\\?");
            this.path = parts[0];       // "/user/create"
            String queryString = parts[1]; // "userId=aaaaa&name=11111&password=222222"

            // 쿼리 스트링을 &와 = 기준으로 쪼개서 맵에 저장
            parseParameters(queryString);
        } else {
            this.path = url;
        }
    }

    private void parseParameters(String queryString) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = (keyValue.length > 1) ? keyValue[1] : "";
            params.put(key, value); // params 맵에 저장
        }
    }

    // 컨트롤러가 호출할 메서드
    public String getParameter(String name) {
        return params.get(name);
    }

    public String getPath() {
        return path;
    }
}