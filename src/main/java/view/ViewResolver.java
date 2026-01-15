package view;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewResolver {
    private static final Logger logger = LoggerFactory.getLogger(ViewResolver.class);
    private static final String DEFAULT_PAGE = "index.html";

    public MyView resolve(String viewName) {
        // 1. 리다이렉트 요청은 경로 계산 없이 즉시 MyView 생성
        if (viewName.startsWith("redirect:")) {
            return new MyView(viewName);
        }

        String path = viewName;

        // 2. 경로 보정: 슬래시로 시작하지 않으면 붙여줌
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        //  기존의 복잡한 substring 로직을 제거했습니다.
        // 컨트롤러가 준 경로(/img/profile/...)를 그대로 믿고 파일 시스템에서 찾습니다.

        // 3. 물리적 파일 위치 확인
        File file = new File("./src/main/resources/static" + path);

        // 4. 디렉토리 요청 처리 (예: /article -> /article/index.html)
        if (file.exists() && file.isDirectory()) {
            path = path.endsWith("/") ? path + DEFAULT_PAGE : path + "/" + DEFAULT_PAGE;
            logger.debug("디렉토리 요청 -> 기본 페이지 전환: {}", path);
        }

        return new MyView(path);
    }
}