package controller;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

public class ControllerHandlerAdapter implements Controller{
    private static final Logger logger = LoggerFactory.getLogger(UserCreateController.class)

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")
        );

        Database.addUser(user);
        logger.debug("회원가입 성공: {}", user);

        return "/index.html";
    }
}
