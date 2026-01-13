package controller;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerMapping {
    private static final Logger logger = LoggerFactory.getLogger(HandlerMapping.class);

    // 주소(URL)를 Key로, 담당 컨트롤러(Controller)를 Value로 저장하는 지도
    private static final Map<String, Controller> mappings = new HashMap<>();

    static {
        // 1. 메인 페이지 및 정적 리소스 (index.html 동적 렌더링 포함)
        mappings.put("/", new ResourceController());
        mappings.put("/index.html", new ResourceController());
        mappings.put("/main", new ResourceController());


        // 2. 유저 관련 컨트롤러
        mappings.put("/user/create", new UserCreateController()); // 회원가입 로직
        mappings.put("/user/login", new LoginController());      // 로그인 로직
        mappings.put("/user/logout", new LogoutController());    // 로그아웃 로직 (새로 추가!)


        // 3. 게시글 관련 컨트롤러
        // 💡 버튼에서 설정한 /article 경로를 등록합니다.
        // 게시글 목록을 보여주거나 글쓰기 폼을 보여주는 역할을 합니다.
        mappings.put("/article", new ArticleController());
        mappings.put("/comment", new CommentController());

        // 4. 댓글 관련 (필요 시 추가)
        // mappings.put("/comment/create", new CommentController());

        logger.info("HandlerMapping 초기화 완료: {}개의 컨트롤러 등록됨", mappings.size());
    }

    /**
     * 요청된 경로에 매핑된 컨트롤러를 찾아 반환합니다.
     */
    public static Controller getHandler(String path) {
        // 정적 리소스(css, js, 이미지 등)는 ResourceController가 기본적으로 담당하도록
        // 매핑에 없는 경로는 null을 반환하여 DispatcherServlet에서 처리하게 하거나,
        // 아래처럼 직접 ResourceController를 지정할 수 있습니다.
        Controller handler = mappings.get(path);

        if (handler == null) {
            // 별도 매핑이 없는 파일 요청 등은 ResourceController가 처리
            return new ResourceController();
        }

        logger.debug("Path '{}'에 매핑된 핸들러 발견: {}", path, handler.getClass().getSimpleName());
        return handler;
    }
}