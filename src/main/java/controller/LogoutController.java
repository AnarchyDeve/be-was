package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import http.HttpSessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        // 1. 요청에 포함된 sid 쿠키를 가져옵니다.
        String sid = request.getCookie("sid");

        if (sid != null) {
            // 2. 서버 메모리(HttpSessions)에서 세션 정보를 삭제합니다.
            // 서버 내부 맵에서 해당 sid와 연결된 정보를 모두 제거합니다.
            HttpSessions.remove(sid);

            // 3. 브라우저의 쿠키를 무효화(청소)합니다.
            // HttpResponse에 만든 expireCookie가 있다면 그것을 쓰고,
            // 없다면 직접 addHeader를 통해 Max-Age=0을 설정합니다.
            response.addHeader("Set-Cookie", "sid=; Path=/; Max-Age=0;");

            logger.debug("로그아웃 성공 - 삭제된 세션 ID: {}", sid);
        }

        // 4. 로그아웃 후 메인 페이지로 리다이렉트합니다.
        // 직접 응답을 전송하므로 null을 리턴하여 ViewResolver의 중복 작동을 막습니다.
        response.sendRedirect(HttpStatus.FOUND, "/index.html");
        return null;
    }
}