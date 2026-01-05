package http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtils {
    /**
     * @param queryString "userId=javajigi&password=password&name=박재성"
     * @return {userId=javajigi, password=password, name=박재성}
     */
    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();

        // 1. & 를 기준으로 먼저 쪼갠다 (각 키=값 쌍을 찾음)
        String[] tokens = queryString.split("&");

        for (String token : tokens) {
            // 2. = 를 기준으로 키와 값을 나눈다
            String[] keyValue = token.split("=");

            // 값이 없는 경우(예: &email= )를 대비한 예외 처리
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }
}