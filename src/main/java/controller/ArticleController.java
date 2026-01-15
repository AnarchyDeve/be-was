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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArticleController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private static final String UPLOAD_DIR = "./src/main/resources/static/img/article/";

    @Override
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getMethod().equals("POST")) {
            return createArticle(request, response);
        }
        return "/article/index.html";
    }

    private String createArticle(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        String contents = request.getParameter("contents");

        // 💡 1. DB에 게시글을 먼저 저장하여 생성된 고유 ID를 가져옵니다.
        Article newArticle = new Article(user.getUserId(), "게시글", contents, null);
        Long articleId = ArticleRepository.save(newArticle);

        // 💡 2. HttpRequest에서 메모리에 저장된 파일 데이터를 꺼냅니다.
        byte[] fileData = request.getFileData("imageFile");
        String originalFileName = request.getFileName("imageFile");

        String finalPath = "/img/article/basic_article.svg"; // 기본 이미지

        if (fileData != null && fileData.length > 0 && articleId != null) {
            String extension = "";
            if (originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // 💡 3. 파일명을 'article_ID.확장자'로 생성
            String saveFileName = "article_" + articleId + extension;
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File targetFile = new File(uploadDir, saveFileName);

            // 💡 4. 물리적 파일 저장
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(fileData);
            }

            finalPath = "/img/article/" + saveFileName;

            // 💡 5. 저장된 최종 경로를 DB에 업데이트
            ArticleRepository.updateImagePath(articleId, finalPath);
            logger.info("게시글 이미지 저장 완료: id={}, path={}", articleId, finalPath);
        } else if (articleId != null) {
            ArticleRepository.updateImagePath(articleId, finalPath);
        }

        return "redirect:/index.html";
    }
}