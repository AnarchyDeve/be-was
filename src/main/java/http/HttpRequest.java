package http;

import db.Database;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpRequest(InputStream in){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();
            if( line == null) return;

            int contentLength = 0; // 어디까지 읽어야 할지를 써놔야하기 때문에 중요함.

            String[] tokens = line.split(" ");
            this.method = tokens[0]; // GET POST 등 어떠한 매서드인지 아는거임

            // ... 기존 코드 생략 ...

            String fullPath = tokens[1]; // 예: /create?userId=javajigi&...
            if (fullPath.contains("?")) {
                String[] parts = fullPath.split("\\?"); // ?를 기준으로 분리
                this.path = parts[0];                   // /create 만 저장
                String queryString = parts[1];          // userId=javajigi&... 부분

                // 여기서 Map으로 변환하는 로직을 수행 (주석으로 말씀하신 내용)
                Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);

                // 만약 path가 "/create" 라면 데이터베이스에 저장하는 로직으로 연결!
                if ("/create".equals(this.path)) {
                    User user = new User(
                            params.get("userId"),
                            params.get("password"),
                            params.get("name"),
                            params.get("email")
                    );
                    Database.addUser(user); // 말씀하신 Database 클래스에 저장
                }
            } else {
                this.path = fullPath;
            }
// ... 헤더 읽기 로직 시작 ...

            while (!(line = br.readLine()).equals("")){
                String[] headerTokens = line.split(": ");
                // : 을 기준으로 나누면
                if (headerTokens.length == 2){
                    headers.put(headerTokens[0], headerTokens[1]);
                }
            }

            if ("POST".equals(method) && headers.containsKey("Content-Length")){
                int length = Integer.parseInt(headers.get("Content-Length"));
                char[] buffer = new char[length];
                br.read(buffer, 0, length);
                this.body = new String(buffer);
             }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


}
