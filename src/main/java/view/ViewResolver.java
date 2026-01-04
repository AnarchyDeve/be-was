package view;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewResolver {
    private static final Logger logger = LoggerFactory.getLogger(ViewResolver.class);
    private static final String DEFAULT_PAGE = "index.html";

    public MyView resolve(String viewName) {
        // 1. 기본 경로 설정
        String path = viewName;

        // 2. 물리적 파일 위치 확인용 객체 생성
        File file = new File("./src/main/resources/static" + path);

        // 3. 만약 경로가 디렉토리(폴더)라면 그 안의 index.html을 가리키도록 수정
        if (file.isDirectory()) {
            // 경로 끝에 /가 없으면 붙여주고 index.html 추가
            path = path.endsWith("/") ? path + DEFAULT_PAGE : path + "/" + DEFAULT_PAGE;
            logger.debug("요청이 디렉토리이므로 기본 페이지로 전환: {}", path);
        }

        return new MyView(path);
    }
}