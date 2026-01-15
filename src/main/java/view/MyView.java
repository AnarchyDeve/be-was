package view;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MyView {
    private static final Logger logger = LoggerFactory.getLogger(MyView.class);
    private static final String DEFAULT_PREFIX = "./src/main/resources/static";
    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(HttpRequest request, HttpResponse response) throws IOException {
        // 1. 리다이렉트 처리 (중요!)
        if (viewPath.startsWith("redirect:")) {
            String redirectPath = viewPath.substring("redirect:".length());
            logger.debug("MyView: 리다이렉트 실행 -> {}", redirectPath);
            response.sendRedirect(HttpStatus.FOUND, redirectPath);
            return;
        }

        // 2. 파일 경로 정규화 (중복 슬래시 방지)
        String normalizedPath = viewPath.startsWith("/") ? viewPath : "/" + viewPath;
        File file = new File(DEFAULT_PREFIX + normalizedPath);

        if (file.exists() && file.isFile()) {
            // 3. 파일 전송
            byte[] body = Files.readAllBytes(file.toPath());

            response.setStatus(HttpStatus.OK);
            // viewPath의 확장자를 통해 Content-Type 설정 (png, svg, css 등)
            response.setContentType(normalizedPath);

            response.send(body);
            logger.debug("MyView: '{}' 파일 전송 성공", normalizedPath);
        } else {
            // 4. 파일이 없을 경우
            logger.error("MyView: 파일을 찾을 수 없음 -> {}", file.getAbsolutePath());
            response.setStatus(HttpStatus.NOT_FOUND);
            response.send("<h1>404 Not Found - MyView can't find the file</h1>".getBytes());
        }
    }
}