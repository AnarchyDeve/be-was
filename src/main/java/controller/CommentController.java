package controller;

import db.ArticleRepository;
import db.CommentRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.Article;
import model.Comment;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CommentController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getMethod().equals("POST")) {
            return createComment(request, response);
        }
        // GET 요청 시 댓글 작성 페이지(/comment/index.html) 반환
        return "/comment/index.html";
    }

    private String createComment(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        // 1. 파라미터 추출
        String contents = request.getParameter("contents");
        String indexStr = request.getParameter("index"); //  현재 보고 있던 글의 인덱스
        int currentIndex = (indexStr != null) ? Integer.parseInt(indexStr) : 0;

        // 2. 현재 인덱스에 해당하는 게시글 찾기
        List<Article> articles = ArticleRepository.findAll();
        if (articles.isEmpty() || currentIndex >= articles.size()) {
            return "redirect:/index.html";
        }
        Article targetArticle = articles.get(currentIndex);

        // 3. 댓글 객체 생성 및 저장 (articleId 포함)
        Comment newComment = new Comment(
                targetArticle.getId(),
                user.getUserId(),
                user.getName(),
                contents
        );
        CommentRepository.addComment(newComment);

        logger.info("댓글 등록 성공: 게시글ID={}, 작성자={}", targetArticle.getId(), user.getUserId());

        // 4.  보던 페이지로 리다이렉트 (index 파라미터 유지)
        return "redirect:/index.html?index=" + currentIndex;
    }
}