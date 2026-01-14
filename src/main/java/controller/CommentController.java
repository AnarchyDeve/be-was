package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.Comment;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommentController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        String method = request.getMethod();

        if (method.equals("POST")) {
            return createComment(request, response);
        }

        // GET 요청 시 댓글 작성 페이지를 보여주려면 (index.html에서 링크 눌렀을 때)
        response.forward("/comment/index.html");
        return null;
    }

    // CommentController.java 내부
    private String createComment(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user"); // 세션에서 로그인 유저 획득

        if (user == null) {
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        String contents = request.getParameter("contents");

        // 💡 유저의 이름을 작성자로 하여 댓글 객체 생성
        Comment newComment = new Comment(user.getName(), contents);
        db.CommentRepository.addComment(newComment); // DB에 저장

        response.sendRedirect(HttpStatus.FOUND, "/index.html");
        return null;
    }
}