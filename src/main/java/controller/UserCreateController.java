package controller;

import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 회원가입(User Creation) 비즈니스 로직을 전담하는 컨트롤러입니다.
 */
public class UserCreateController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        // 1. HttpRequest의 파라미터 맵에서 유저 정보를 추출합니다.
        // 이미 HttpRequest에서 파싱 로직이 완성되어 있으므로 getParameter를 사용합니다.
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")
        );

        // 2. 비즈니스 로직 수행: 데이터베이스에 유저 저장
        UserRepository.addUser(user);
        logger.info("새로운 회원가입 성공: {}", user.getUserId());

        // 3. 작업 완료 후 이동할 논리적인 뷰 이름을 반환합니다.
        // 회원가입 성공 후에는 메인 페이지(index.html)로 이동하도록 설계합니다.
        // 포워드 방식을 리다이렉트 방식으로 바꿔봄
        // return "/index.html";
        return "redirect:/index.html";
    }
}