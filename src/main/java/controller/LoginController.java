package controller;

import db.Database;
import http.*;
import model.User;

public class LoginController implements Controller {
    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        // 1. 데이터베이스에서 해당 ID의 유저를 찾습니다.
        User user = Database.findUserById(userId);

        // 2. 유저가 존재하고, 비밀번호가 일치하는지 확인합니다.
        if (user != null && user.getPassword().equals(password)) {

            // [수정 핵심] 랜덤 UUID 대신, 관리소(HttpSessions)에게
            // "이 유저(userId)의 세션을 가져와, 없으면 만들고" 라고 요청합니다.
            HttpSession session = HttpSessions.getOrCreateSession(userId);

            // 세션에 유저 정보 저장 (기존 세션이면 덮어쓰기 됨)
            session.setAttribute("user", user);

            // [중요] 쿠키에는 세션의 ID를 담아 보냅니다.
            // (HttpSessions 구현에 따라 이 ID는 userId와 같을 수 있습니다)
            response.addHeader("Set-Cookie", "sid=" + session.getId() + "; Path=/");

            System.out.println("로그인 성공: " + userId);
            System.out.println("발급된 세션 ID: " + session.getId());

            return "redirect:/index.html";
        } else {
            System.out.println("로그인 실패: " + userId);
            return "redirect:/user/login_failed.html";
        }
    }
}