package controller;

import db.UserRepository;
import http.*;
import model.User;

public class LoginController implements Controller {
    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        User user = UserRepository.findUserById(userId);

        // [체크 1] 유저가 아예 존재하지 않는 경우 (에러 코드 1)
        if (user == null) {
            return "redirect:/login/index.html?error=1";
        }

        // [체크 2] 비밀번호가 맞지 않는 경우 (에러 코드 2)
        if (!user.getPassword().equals(password)) {
            return "redirect:/login/index.html?error=2";
        }

        // 로그인 성공
        HttpSession session = HttpSessions.getOrCreateSession(userId);
        session.setAttribute("user", user);
        response.addHeader("Set-Cookie", "sid=" + session.getId() + "; Path=/");

        return "redirect:/index.html";
    }
}