package controller;

import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 정적 리소스(HTML, CSS, JS, 이미지 등) 요청을 처리하는 컨트롤러입니다.
 * 특별한 비즈니스 로직 없이, 요청 경로를 뷰 이름으로 반환하여
 * ViewResolver가 해당 파일을 찾도록 유도합니다.
 */
public class ResourceController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Override
    public String process(HttpRequest request, HttpResponse response) {
        String path = request.getPath();

        // 루트(/) 요청인 경우 기본적으로 index.html로 이동하도록 설정
        if (path.equals("/")) {
            path = "/index.html";
        }

        logger.debug("정적 리소스 요청 처리: {}", path);

        // 요청받은 경로를 논리적 뷰 이름으로 반환
        return path;
    }
}