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
        // 1. 회원가입 컨트롤러 등록
        mappings.put("/create", new UserCreateController());

        // 2. 기타 정적 페이지 전용 컨트롤러 등록 (필요 시 추가)
        // mappings.put("/login", new LoginController());

        logger.info("HandlerMapping 초기화 완료: {}개의 컨트롤러 등록됨", mappings.size());
    }

    /**
     * 요청된 경로에 매핑된 컨트롤러를 찾아 반환합니다.
     * @param path 요청 주소 (예: /create)
     * @return 매핑된 Controller 객체, 없으면 null 반환
     */
    public static Controller getHandler(String path) {
        Controller handler = mappings.get(path);

        if (handler != null) {
            logger.debug("Path '{}'에 매핑된 핸들러 발견: {}", path, handler.getClass().getSimpleName());
        }

        return handler;
    }
}