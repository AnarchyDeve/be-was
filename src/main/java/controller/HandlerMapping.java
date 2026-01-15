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
        mappings.put("/user/create", new UserCreateController());   // 회원가입 로직
        mappings.put("/user/login", new LoginController());         // 로그인 로직
        mappings.put("/user/logout", new LogoutController());       // 로그아웃 로직

        // 💡 마이페이지 정보 수정 경로 추가
        mappings.put("/user/update", new UserUpdateController());   // 프로필/비번 수정 로직 (새로 추가!)

        // 마이페이지 폼 이동을 위한 매핑 (필요 시)
        mappings.put("/mypage", new ResourceController());

        // 3. 게시글 관련 컨트롤러
        mappings.put("/article", new ArticleController());
        mappings.put("/comment", new CommentController());

        logger.info("HandlerMapping 초기화 완료: {}개의 컨트롤러 등록됨", mappings.size());
    }

    /**
     * 요청된 경로에 매핑된 컨트롤러를 찾아 반환합니다.
     */
    public static Controller getHandler(String path) {
        // 1. 등록된 매핑 정보에서 컨트롤러를 찾습니다.
        Controller handler = mappings.get(path);

        // 2. 매핑에 없는 경로(이미지, CSS 등 정적 파일)는 ResourceController가 기본적으로 담당합니다.
        if (handler == null) {
            return new ResourceController();
        }

        logger.debug("Path '{}'에 매핑된 핸들러 발견: {}", path, handler.getClass().getSimpleName());
        return handler;
    }
}