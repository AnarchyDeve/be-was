package controller;

import db.ArticleRepository;
import db.CommentRepository;
import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.Article;
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
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();

        if (path.equals("/") || path.equals("/index.html")) {
            String indexStr = request.getParameter("index");
            int currentIndex = (indexStr != null) ? Integer.parseInt(indexStr) : 0;
            return handleIndexHtml(request, response, currentIndex);
        }

        if (path.equals("/mypage") || path.equals("/mypage/index.html")) {
            return handleMyPage(request, response);
        }

        return path;
    }

    private String handleMyPage(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        try {
            File file = new File(STATIC_PATH + "/mypage/index.html");
            if (!file.exists()) return "/404.html";

            String html = new String(Files.readAllBytes(file.toPath()), "UTF-8");

            html = html.replace("{{user_name}}", user.getName())
                    .replace("{{user_profile_image}}", user.getProfileImage())
                    .replace("{{header_menu}}", buildHeaderMenu(session));

            response.forwardBody(html.getBytes("UTF-8"));
            return null;
        } catch (IOException e) {
            logger.error("Error rendering mypage", e);
            return null;
        }
    }

    private String handleIndexHtml(HttpRequest request, HttpResponse response, int currentIndex) {
        try {
            File file = new File(STATIC_PATH + "/index.html");
            String html = new String(Files.readAllBytes(file.toPath()), "UTF-8");

            html = html.replace("{{header_menu}}", buildHeaderMenu(request.getSession()));

            List<Article> articles = ArticleRepository.findAll();
            StringBuilder contentBuilder = new StringBuilder();

            if (articles.isEmpty()) {
                contentBuilder.append("<div class='empty-feed'>업로드된 게시물이 없습니다.</div>");
                html = html.replace("{{prev_disabled}}", "disabled")
                        .replace("{{next_disabled}}", "disabled")
                        .replace("{{comment_btn_disabled}}", "disabled")
                        .replace("{{prev_url}}", "#")
                        .replace("{{next_url}}", "#")
                        .replace("{{current_index}}", "0");
            } else {
                if (currentIndex < 0) currentIndex = 0;
                if (currentIndex >= articles.size()) currentIndex = articles.size() - 1;

                Article current = articles.get(currentIndex);
                User author = UserRepository.findUserById(current.getWriter());
                String authorProfile = (author != null) ? author.getProfileImage() : "/img/profile/basic_profileImage.svg";
                List<Comment> comments = CommentRepository.findByArticleId(current.getId());

                contentBuilder.append("<div class='post'>");
                contentBuilder.append("  <div class='post__account'>");
                contentBuilder.append("    <img class='post__account__img' src='").append(authorProfile).append("' />");
                contentBuilder.append("    <p class='post__account__nickname'>").append(current.getWriter()).append("</p>");
                contentBuilder.append("  </div>");
                contentBuilder.append("  <img class='post__img' src='").append(current.getImagePath()).append("' />");

                contentBuilder.append("  <div class='post__menu'>");
                contentBuilder.append("    <ul class='post__menu__personal' style='display: flex; list-style: none; padding: 0; margin: 10px 0; gap: 20px;'>");
                contentBuilder.append("      <li style='display: flex; align-items: center;'>");
                contentBuilder.append("        <button class='post__menu__btn' style='display: flex; align-items: center; background: none; border: none; padding: 0; gap: 5px;'>");
                contentBuilder.append("          <img src='/img/like.svg' style='width: 24px; height: 24px;' />");
                contentBuilder.append("          <span style='font-size: 14px; font-weight: bold;'>").append(current.getLikeCount()).append("</span>");
                contentBuilder.append("        </button>");
                contentBuilder.append("      </li>");
                contentBuilder.append("      <li style='display: flex; align-items: center;'>");
                contentBuilder.append("        <div class='post__menu__btn' style='display: flex; align-items: center; gap: 5px;'>");
                contentBuilder.append("          <img src='/img/comment.svg' style='width: 24px; height: 24px;' />");
                contentBuilder.append("          <span style='font-size: 14px; font-weight: bold;'>").append(comments.size()).append("</span>");
                contentBuilder.append("        </div>");
                contentBuilder.append("      </li>");
                contentBuilder.append("    </ul>");
                contentBuilder.append("  </div>");
                contentBuilder.append("  <p class='post__article'>").append(current.getContents()).append("</p>");
                contentBuilder.append("</div>");

                //  [수정] 댓글 목록 출력 (최대 3개)
                contentBuilder.append("<ul class='comment'>").append(buildCommentListHtml(comments)).append("</ul>");

                //  [추가] 댓글이 3개보다 많으면 '모든 댓글 보기' 버튼 생성
                if (comments.size() > 3) {
                    contentBuilder.append("<button id='show-all-btn' class='btn btn_ghost btn_size_m' style='width:100%; text-align:left; padding:10px 0;'>");
                    contentBuilder.append("모든 댓글 보기(").append(comments.size() - 3 ).append("개)");
                    contentBuilder.append("</button>");
                }

                String prevUrl = "/index.html?index=" + (currentIndex + 1);
                String nextUrl = "/index.html?index=" + (currentIndex - 1);
                String prevStatus = (currentIndex < articles.size() - 1) ? "" : "disabled";
                String nextStatus = (currentIndex > 0) ? "" : "disabled";

                html = html.replace("{{prev_url}}", prevUrl)
                        .replace("{{next_url}}", nextUrl)
                        .replace("{{prev_disabled}}", prevStatus)
                        .replace("{{next_disabled}}", nextStatus)
                        .replace("{{comment_btn_disabled}}", "")
                        .replace("{{current_index}}", String.valueOf(currentIndex));
            }

            html = html.replace("{{main_content}}", contentBuilder.toString());
            response.forwardBody(html.getBytes("UTF-8"));
            return null;
        } catch (IOException e) {
            logger.error("Error rendering index.html", e);
            return null;
        }
    }

    private String buildCommentListHtml(List<Comment> comments) {
        StringBuilder sb = new StringBuilder();

        //  [수정] 최대 3개까지만 반복문 실행
        int displayCount = Math.min(comments.size(), 3);

        for (int i = 0; i < displayCount; i++) {
            Comment c = comments.get(i);
            User writer = UserRepository.findUserById(c.getUserId());
            String profilePath = (writer != null) ? writer.getProfileImage() : "/img/profile/basic_profileImage.svg";

            sb.append("<li class='comment__item'>");
            sb.append("  <div class='comment__item__user'>");
            sb.append("    <img class='comment__item__user__img' src='").append(profilePath).append("' />");
            sb.append("    <p class='comment__item__user__nickname'>").append(c.getWriterName()).append("</p>");
            sb.append("  </div>");
            sb.append("  <p class='comment__item__article'>").append(c.getContents()).append("</p>");
            sb.append("</li>");
        }
        return sb.toString();
    }

    private String buildHeaderMenu(HttpSession session) {
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user != null) {
            return "<li class='header__menu__item'><a href='/mypage' style='text-decoration:none; color:inherit; font-weight:bold;'>안녕하세요 " + user.getName() + "님</a></li>" +
                    "<li class='header__menu__item'><a class='btn btn_contained btn_size_s' href='/article'>글쓰기</a></li>" +
                    "<li class='header__menu__item'><a class='btn btn_ghost btn_size_s' href='/user/logout'>로그아웃</a></li>";
        }
        return "<li class='header__menu__item'><a class='btn btn_contained btn_size_s' href='/login'>로그인</a></li>" +
                "<li class='header__menu__item'><a class='btn btn_ghost btn_size_s' href='/registration'>회원 가입</a></li>";
    }
}