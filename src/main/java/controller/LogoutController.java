package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import http.HttpSessions;
import http.HttpSession;

public class LogoutController implements Controller {
    @Override
    public String process(HttpRequest request, HttpResponse response) {
        // 1. 요청에 포함된 sid 쿠키를 가져옵니다.
        String sid = request.getCookie("sid");

        if (sid != null) {
            // 2. 서버 메모리(HttpSessions)에서 세션 정보를 삭제합니다.
            // 우리가 만든 HttpSessions.remove(sid)가 내부의 두 맵을 모두 정리합니다.
            HttpSessions.remove(sid);

            // 3. 브라우저에게 "이 쿠키는 이제 끝났어"라고 알려줍니다. (쿠키 삭제)
            // Max-Age=0을 주면 브라우저가 즉시 쿠키를 폐기합니다.
            response.addHeader("Set-Cookie", "sid=; Path=/; Max-Age=0");

            System.out.println("로그아웃 성공 - 삭제된 SID: " + sid);
        }

        // 4. 로그아웃 후 메인 페이지로 보냅니다.
        return "redirect:/index.html";
    }
}