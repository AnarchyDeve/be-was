package controller;

import db.CommentRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.Comment;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ResourceController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
    private static final String STATIC_PATH = "./src/main/resources/static";

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String path = request.getPath();

        // 기본 경로 처리
        if (path.equals("/")) {
            path = "/index.html";
        }

        // index.html 요청일 때만 동적 치환(헤더 메뉴 및 댓글 목록)을 수행합니다.
        if (path.equals("/index.html")) {
            return handleIndexHtml(request, response);
        }

        // 그 외 정적 파일(.css, .img 등)은 경로만 반환하여 ViewResolver가 처리하게 합니다.
        return path;
    }

    /**
     * index.html 파일을 읽어 동적 요소({{header_menu}}, {{comment_list}})를 치환한 뒤 응답합니다.
     */
    private String handleIndexHtml(HttpRequest request, HttpResponse response) {
        try {
            File file = new File(STATIC_PATH + "/index.html");
            String html = new String(Files.readAllBytes(file.toPath()), "UTF-8");

            // 1. 헤더 메뉴 치환
            html = html.replace("{{header_menu}}", buildHeaderMenu(request.getSession()));

            // 2. DB에서 댓글 가져오기
            List<Comment> dbComments = CommentRepository.findAll();

            // 3. 💡 새로운 댓글들을 'hidden' 클래스를 넣어서 생성
            StringBuilder sb = new StringBuilder();
            for (Comment comment : dbComments) {
                sb.append("<li class=\"comment__item hidden\">"); // 💡 hidden 추가
                sb.append("    <div class=\"comment__item__user\">");
                sb.append("        <img class=\"comment__item__user__img\" src=\"./img/default-profile.png\" />");
                sb.append("        <p class=\"comment__item__user__nickname\">").append(comment.getWriterName()).append("</p>");
                sb.append("    </div>");
                sb.append("    <p class=\"comment__item__article\">").append(comment.getContents()).append("</p>");
                sb.append("</li>");
            }
            html = html.replace("{{comment_list}}", sb.toString());

            // 4. 💡 전체 숨겨진 댓글 개수 계산 (기존 정적 3개 + DB 댓글 수)
            int totalHiddenCount = 3 + dbComments.size();
            html = html.replace("{{comment_count}}", String.valueOf(totalHiddenCount));

            response.forwardBody(html.getBytes("UTF-8"));
            return null;
        } catch (IOException e) {
            logger.error("Error rendering index.html: {}", e.getMessage());
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");
            return null;
        }
    }

    /**
     * DB(CommentRepository)에서 댓글 목록을 가져와 HTML 태그 뭉치로 생성합니다.
     */
    private String buildCommentListHtml() {
        // 💡 CommentRepository.findAll()을 통해 DB에 저장된 댓글 리스트를 가져옵니다.
        List<Comment> comments = CommentRepository.findAll();
        StringBuilder sb = new StringBuilder();

        for (Comment comment : comments) {
            sb.append("<li class=\"comment__item\">");
            sb.append("    <div class=\"comment__item__user\">");
            sb.append("        <img class=\"comment__item__user__img\" src=\"./img/default-profile.png\" />");
            // 작성자 이름과 본문을 DB 데이터로 채웁니다.
            sb.append("        <p class=\"comment__item__user__nickname\">").append(comment.getWriterName()).append("</p>");
            sb.append("    </div>");
            sb.append("    <p class=\"comment__item__article\">").append(comment.getContents()).append("</p>");
            sb.append("</li>");
        }
        return sb.toString();
    }

    /**
     * 로그인 상태에 따라 사용자 이름 또는 로그인 버튼을 반환합니다.
     */
    private String buildHeaderMenu(HttpSession session) {
        StringBuilder sb = new StringBuilder();
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user != null) {
            // 로그인 상태: 안녕하세요 이름님 | 글쓰기 | 로그아웃
            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_size_s\" style=\"color: #000; font-weight: bold;\">");
            sb.append("안녕하세요 ").append(user.getName()).append("님");
            sb.append("  </a>");
            sb.append("</li>");

            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_contained btn_size_s\" href=\"/article\">글쓰기</a>");
            sb.append("</li>");

            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_ghost btn_size_s\" href=\"/user/logout\">로그아웃</a>");
            sb.append("</li>");
        } else {
            // 미인증 상태: 로그인 | 회원 가입
            sb.append("<li class=\"header__menu__item\"><a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a></li>");
            sb.append("<li class=\"header__menu__item\"><a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">회원 가입</a></li>");
        }
        return sb.toString();
    }
}