package controller;

import db.UserRepository;
import http.*;
import model.User;

public class LoginController implements Controller {
    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        // 1. 데이터베이스에서 해당 ID의 유저를 조회합니다.
        User user = UserRepository.findUserById(userId);

        // 2. 검증 로직을 단계별로 나눕니다.

        // [체크 1] 유저가 아예 존재하지 않는 경우
        if (user == null) {
            System.out.println("로그인 실패: 존재하지 않는 아이디입니다. (" + userId + ")");
            // 여기서 클라이언트에게 에러 메시지를 보낼 수 있도록 세팅하거나 쿼리 파라미터를 붙일 수 있습니다.
            return "redirect:/login/index.html";
        }

        // [체크 2] 아이디는 있으나 비밀번호가 일치하지 않는 경우
        if (!user.getPassword().equals(password)) {
            System.out.println("로그인 실패: 비밀번호가 맞지 않습니다. (" + userId + ")");
            return "redirect:/login/index.html";
        }

        // 3. 로그인이 성공한 경우 (위의 if문들을 모두 통과함)
        HttpSession session = HttpSessions.getOrCreateSession(userId);
        session.setAttribute("user", user);
        response.addHeader("Set-Cookie", "sid=" + session.getId() + "; Path=/");

        System.out.println("로그인 성공: " + userId);
        return "redirect:/index.html";
    }
}