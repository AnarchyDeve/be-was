package controller;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 정적 리소스(HTML, CSS, JS, 이미지 등) 요청을 처리하는 컨트롤러입니다.
 * index.html의 경우 세션 상태에 따라 동적으로 HTML을 가공하여 응답합니다.
 */
public class ResourceController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    // MyView 및 ViewResolver와 일치시킨 물리적 경로
    private static final String STATIC_PATH = "./src/main/resources/static";

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String path = request.getPath();

        // 1. 루트(/) 요청 시 기본 페이지 설정
        if (path.equals("/")) {
            path = "/index.html";
        }

        // 2. index.html 요청인 경우에만 특별하게 동적 가공(Rendering) 처리
        if (path.equals("/index.html")) {
            return handleIndexHtml(request, response);
        }

        // 3. index.html이 아닌 다른 정적 파일(CSS, JS, 이미지 등)은
        // ViewResolver가 처리하도록 파일 경로(ViewName)를 그대로 리턴
        logger.debug("정적 리소스 요청 처리 (ViewResolver 위임): {}", path);
        return path;
    }

    /**
     * index.html 파일을 읽어서 세션 정보에 따라 {{header_menu}} 부분을 치환한 후 직접 응답합니다.
     */
    private String handleIndexHtml(HttpRequest request, HttpResponse response) {
        try {
            // MyView의 물리 경로와 동일한 위치에서 파일을 찾습니다.
            File file = new File(STATIC_PATH + "/index.html");

            if (!file.exists()) {
                logger.error("파일을 찾을 수 없습니다: {}", file.getAbsolutePath());
                // 1. 여기서 바로 404 전송
                byte[] errorBody = "<h1>404 Not Found</h1>".getBytes(); // 혹은 404용 파일 읽기
                response.setStatus(HttpStatus.NOT_FOUND);
                response.send(errorBody);
                return null; // 바로 끝냄
            }

            // 1. 원본 HTML 파일 내용 읽기
            String html = new String(Files.readAllBytes(file.toPath()), "UTF-8");

            // 2. 세션 확인 및 메뉴 HTML 생성
            HttpSession session = request.getSession();
            String menuHtml = buildHeaderMenu(session);

            // 3. 템플릿 태그 {{header_menu}} 치환
            String renderedHtml = html.replace("{{header_menu}}", menuHtml);

            // 4. 가공된 바디를 HttpResponse를 통해 직접 전송
            // 이 시점에 response 내부에 상태코드와 Content-Type 헤더가 설정되고 전송까지 완료됩니다.
            response.forwardBody(renderedHtml.getBytes("UTF-8"));

            // 5. 직접 전송을 마쳤으므로 ViewResolver가 작동하지 않도록 null을 반환합니다.
            logger.debug("index.html 동적 렌더링 및 직접 전송 완료");
            return null;

        } catch (IOException e) {
            logger.error("서버 오류 발생: {}", e.getMessage());
            // 2. 여기서 바로 500 전송
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.send("<h1>500 Internal Server Error</h1>".getBytes());
            return null;
        }
    }

    /**
     * 세션 상태에 따라 상단 헤더 메뉴에 들어갈 HTML 코드를 생성합니다.
     */
    private String buildHeaderMenu(HttpSession session) {
        StringBuilder sb = new StringBuilder();

        // 세션에 로그인한 유저 정보("user" 속성)가 있는지 확인
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");

            // [로그인 상태] 사용자 이름(마이페이지 링크) + 로그아웃 버튼
            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_size_s\" href=\"/mypage\" style=\"color: #000; font-weight: bold;\">");
            sb.append(user.getName()).append("님");
            sb.append("  </a>");
            sb.append("</li>");
            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_ghost btn_size_s\" href=\"/user/logout\">로그아웃</a>");
            sb.append("</li>");
        } else {
            // [미로그인 상태] 로그인 버튼 + 회원 가입 버튼
            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a>");
            sb.append("</li>");
            sb.append("<li class=\"header__menu__item\">");
            sb.append("  <a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">회원 가입</a>");
            sb.append("</li>");
        }
        return sb.toString();
    }
}