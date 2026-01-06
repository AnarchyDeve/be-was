package controller;

import db.Database;
import http.*;
import model.User;

import java.util.UUID;

public class    LoginController implements Controller {
    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
// 2. 데이터베이스에서 해당 ID의 유저를 찾습니다.
        User user = Database.findUserById(userId);

        // 3. 유저가 존재하고, 비밀번호가 일치하는지 확인합니다.
        if (user != null && user.getPassword().equals(password)) {
            String sessionId = UUID.randomUUID().toString();
            HttpSession session = new HttpSessionImpl(sessionId);

            session.setAttribute("user", user);

            HttpSessions.addSession(session);

            response.addHeader("Set-Cookie", "sid=" + sessionId + "; Path=/");

            System.out.println("userId = " + userId);
            System.out.println("sessionId = " + sessionId);
            return "redirect:/index.html";
        } else {
            // [실패] 아이디가 없거나 비번이 틀리면 실패 페이지로 보냅니다.
            System.out.println("로그인 실패: " + userId);
            return "redirect:/login.html";
        }
    }
}
