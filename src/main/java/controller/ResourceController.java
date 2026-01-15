package controller;

import db.ArticleRepository;
import db.CommentRepository;
import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
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
    public String process(HttpRequest request, HttpResponse response) {
        String path = request.getPath();

        if (path.equals("/") || path.equals("/index.html")) {
            String indexStr = request.getParameter("index");
            int currentIndex = (indexStr != null) ? Integer.parseInt(indexStr) : 0;
            return handleIndexHtml(request, response, currentIndex);
        }

        return path;
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
                String authorProfile = (author != null) ? author.getProfileImagePath() : "/img/profile/basic_profileImage.svg";
                List<Comment> comments = CommentRepository.findByArticleId(current.getId());

                // --- 게시물 HTML 조립 ---
                contentBuilder.append("<div class='post'>");
                contentBuilder.append("  <div class='post__account'>");
                contentBuilder.append("    <img class='post__account__img' src='").append(authorProfile).append("' />");
                contentBuilder.append("    <p class='post__account__nickname'>").append(current.getWriter()).append("</p>");
                contentBuilder.append("  </div>");
                contentBuilder.append("  <img class='post__img' src='").append(current.getImagePath()).append("' />");

                // 💡 [수정 포인트] 아이콘과 숫자를 가로로 정렬하기 위한 Flexbox 구조
                contentBuilder.append("  <div class='post__menu'>");
                contentBuilder.append("    <ul class='post__menu__personal' style='display: flex; list-style: none; padding: 0; margin: 10px 0; gap: 20px;'>");

                // 좋아요 섹션
                contentBuilder.append("      <li style='display: flex; align-items: center;'>");
                contentBuilder.append("        <button class='post__menu__btn' onclick='increaseLike()' style='display: flex; align-items: center; background: none; border: none; cursor: pointer; padding: 0; gap: 5px;'>");
                contentBuilder.append("          <img src='/img/like.svg' style='width: 24px; height: 24px;' />");
                contentBuilder.append("          <span id='like-count' style='font-size: 14px; font-weight: bold; color: #262626;'>").append(current.getLikeCount()).append("</span>");
                contentBuilder.append("        </button>");
                contentBuilder.append("      </li>");

                // 댓글 수 섹션
                contentBuilder.append("      <li style='display: flex; align-items: center;'>");
                contentBuilder.append("        <div class='post__menu__btn' style='display: flex; align-items: center; gap: 5px;'>");
                contentBuilder.append("          <img src='/img/comment.svg' style='width: 24px; height: 24px;' />");
                contentBuilder.append("          <span style='font-size: 14px; font-weight: bold; color: #262626;'>").append(comments.size()).append("</span>");
                contentBuilder.append("        </div>");
                contentBuilder.append("      </li>");

                contentBuilder.append("    </ul>");
                contentBuilder.append("  </div>");

                contentBuilder.append("  <p class='post__article'>").append(current.getContents()).append("</p>");
                contentBuilder.append("</div>");

                contentBuilder.append("<ul class='comment'>").append(buildCommentListHtml(comments)).append("</ul>");
                if (comments.size() > 3) {
                    contentBuilder.append("<button id='show-all-btn' class='btn btn_ghost btn_size_m'>모든 댓글 보기(").append(comments.size()).append("개)</button>");
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
        for (Comment c : comments) {
            User writer = UserRepository.findUserById(c.getUserId());
            String profilePath = (writer != null) ? writer.getProfileImagePath() : "/img/profile/basic_profileImage.svg";

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