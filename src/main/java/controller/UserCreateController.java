package controller;

import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCreateController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String profileImage = request.getParameter("profileImage");

        // 1. 길이 검사 (아이디, 비밀번호, 이름 4글자 이상)
        if (userId.length() < 4 || password.length() < 4 || name.length() < 4) {
            return "redirect:/registration/index.html?error=length";
        }

        // 2. 아이디 중복 검사
        if (UserRepository.findUserById(userId) != null) {
            return "redirect:/registration/index.html?error=duplicate_id";
        }

        // 3. 이름(닉네임) 중복 검사 (UserRepository에 findByName이 있다고 가정)
        // 만약 없다면 UserRepository.findAll()로 리스트를 받아와서 체크해야 합니다.
        if (UserRepository.findAll().stream().anyMatch(u -> u.getName().equals(name))) {
            return "redirect:/registration/index.html?error=duplicate_name";
        }

        User user = new User(userId, password, name, email, profileImage);
        UserRepository.addUser(user);

        logger.info("회원가입 성공: {}", userId);
        return "redirect:/login/index.html"; // 가입 성공 시 로그인 페이지로
    }
}