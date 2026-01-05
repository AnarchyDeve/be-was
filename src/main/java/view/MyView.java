package view;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyView {
    private static final Logger logger = LoggerFactory.getLogger(MyView.class);
    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    /**
     * 실제로 파일을 읽어서 응답을 보냅니다. (서블릿의 핵심 로직 분리)
     */
    public void render(HttpRequest request, HttpResponse response) throws IOException {
        // 정적 리소스가 저장된 기본 경로 (src/main/resources/static)
        File file = new File("./src/main/resources/static" + viewPath);

        if (file.exists()) {
            // 1. 파일 데이터를 읽어옵니다.
            byte[] body = Files.readAllBytes(file.toPath());

            // 2. HttpResponse 객체에 상태값과 콘텐츠 타입을 설정합니다.
            response.setStatus(HttpStatus.OK);
            response.setContentType(viewPath); // 확장자를 보고 MIME 타입을 자동 결정하게 구현된 메서드 활용

            // 3. 최종적으로 전송합니다.
            response.send(body);
            logger.debug("MyView: '{}' 파일 전송 완료", viewPath);
        } else {
            // 파일이 없는 경우 404 에러 처리
            response.setStatus(HttpStatus.NOT_FOUND);
            response.send("<h1>404 Not Found</h1>".getBytes());
            logger.error("MyView: '{}' 파일을 찾을 수 없습니다.", viewPath);
        }
    }
}