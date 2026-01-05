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
            String line = br.readLine();
            if (line == null) return;

            // 1. Request Line 분리
            String[] tokens = line.split(" ");
            this.method = tokens[0];
            String url = tokens[1];

            // 2. Header 읽기 및 Content-Length 추출 (대소문자 무시)
            int contentLength = 0;
            while ((line = br.readLine()) != null && !line.equals("")) {
                // 헤더 이름을 소문자로 변환하여 비교 (Content-Length, content-length 등 대응)
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // 3. 데이터 파싱 (GET vs POST)
            if ("GET".equals(method)) {
                parseGetRequest(url);
            } else if ("POST".equals(method)) {
                this.path = url;
                parsePostRequest(br, contentLength);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseGetRequest(String url) {
        if (url.contains("?")) {
            int index = url.indexOf("?");
            this.path = url.substring(0, index);
            String queryString = url.substring(index + 1);
            parseParameters(queryString);
        } else {
            this.path = url;
        }
    }

    private void parsePostRequest(BufferedReader br, int contentLength) throws Exception {
        if (contentLength > 0) {
            char[] body = new char[contentLength];
            // read()는 읽은 문자 수를 반환하며, 실제 바디를 body 배열에 채움
            int readCount = br.read(body, 0, contentLength);
            if (readCount == contentLength) {
                String bodyData = new String(body);
                parseParameters(bodyData);
            }
        }
    }

    private void parseParameters(String queryString) {
        if (queryString == null || queryString.isEmpty()) return;

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length >= 2) {
                params.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                params.put(keyValue[0], "");
            }
        }
    }

    public String getParameter(String name) {
        return params.get(name);
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}