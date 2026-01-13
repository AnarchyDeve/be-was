package controller;

import db.ArticleRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ArticleController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private final ArticleRepository articleRepository = new ArticleRepository();

    @Override
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        String method = request.getMethod();

        // 💡 GET 요청: 글쓰기 페이지(폼)를 보여줍니다.
        if (method.equals("GET")) {
            return showWriteForm(request, response);
        }
        // 💡 POST 요청: 작성한 글과 이미지를 저장합니다.
        else if (method.equals("POST")) {
            return createArticle(request, response);
        }

        // 지원하지 않는 메서드일 경우 메인으로 리다이렉트
        response.sendRedirect(HttpStatus.FOUND, "/index.html");
        return null;
    }

    /**
     * 글쓰기 폼(HTML) 응답 처리
     */
    private String showWriteForm(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();

        // 1. 보안 체크: 로그인하지 않은 사용자는 글을 쓸 수 없습니다.
        if (session == null || session.getAttribute("user") == null) {
            logger.debug("비로그인 사용자 접근 - 로그인 페이지로 이동시킵니다.");
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        // 2. 로그인 상태라면 글쓰기 페이지(/article/index.html)를 보여줍니다.
        // 💡 주의: 파일 경로가 src/main/resources/static/article/index.html 인지 확인하세요.
        response.forward("/article/index.html");
        return null;
    }

    /**
     * 게시글 데이터 DB 저장 처리
     */
    private String createArticle(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 1. 다시 한번 유저 세션을 확인합니다.
        if (user == null) {
            logger.warn("세션 만료 또는 비정상 접근으로 저장 실패");
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        // 2. 파라미터 추출 (제목, 본문)
        String title = request.getParameter("title");
        String contents = request.getParameter("contents");

        // 3. 이미지 파일 경로 추출
        // 💡 HttpRequest에서 멀티파트 파싱 후 저장된 파일의 경로를 가져옵니다.
        String imagePath = request.getSaveFilePath("imageFile");

        // 만약 이미지가 없다면 기본 이미지를 설정합니다.
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "/img/default-post.png";
        }

        // 4. Article 객체 생성 및 리포지토리를 통한 DB 저장
        Article article = new Article(user.getUserId(), title, contents, imagePath);
        articleRepository.save(article);

        logger.info("게시글 저장 성공! 작성자: {}, 제목: {}", user.getName(), title);

        // 5. 저장 완료 후 메인 페이지로 리다이렉트 (PRG 패턴)
        response.sendRedirect(HttpStatus.FOUND, "/index.html");
        return null;
    }
}