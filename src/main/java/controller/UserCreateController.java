package controller;

import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserCreateController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getMethod().equals("GET")) {
            return "/registration/index.html";
        }
        return createUser(request, response);
    }

    private String createUser(HttpRequest request, HttpResponse response) throws IOException {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        // 1. 길이 유효성 검사 (HTML 요구사항인 4자 이상에 맞춤)
        if (isShort(userId) || isShort(password) || isShort(name)) {
            response.sendRedirect(HttpStatus.FOUND, "/registration/index.html?error=length");
            return null;
        }

        // 2. 아이디 중복 체크
        if (UserRepository.findUserById(userId) != null) {
            response.sendRedirect(HttpStatus.FOUND, "/registration/index.html?error=duplicate_id");
            return null;
        }

        // 3. 닉네임 중복 체크 (UserRepository에 findByName이 있다고 가정하거나 findAll로 체크)
        if (isDuplicateName(name)) {
            response.sendRedirect(HttpStatus.FOUND, "/registration/index.html?error=duplicate_name");
            return null;
        }

        // 4. 유저 저장
        User user = new User(userId, password, name, email, "/img/profile/basic_profileImage.svg");
        try {
            UserRepository.addUser(user);
            logger.info("회원가입 성공: {}", userId);
        } catch (Exception e) {
            logger.error("DB 저장 에러", e);
            return "redirect:/registration/index.html?error=db";
        }

        return "redirect:/index.html";
    }

    private boolean isShort(String input) {
        return input == null || input.trim().length() < 4;
    }

    private boolean isDuplicateName(String name) {
        return UserRepository.findAll().stream()
                .anyMatch(u -> u.getName().equals(name));
    }
}