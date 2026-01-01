package webserver;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // 1. HttpRequest 생성 (이 안에서 첫 줄, 헤더, 바디 파싱이 모두 일어납니다)
            HttpRequest request = new HttpRequest(in);

            // 2. HttpResponse 생성 (OutputStream을 품고 응답 준비)
            HttpResponse response = new HttpResponse(out);

            String path = request.getPath();

            // 3. 루트 경로("/") 처리
            if (path.equals("/")) {
                path = "/index.html";
            }

            // 4. 경로 보정 로직 (이미지 등)
            String resourcePath = getResourcePath(path);

            // 5. 파일 존재 여부에 따른 응답 처리
            File file = new File("./src/main/resources/static" + resourcePath);

            if (file.exists()) {
                byte[] body = Files.readAllBytes(file.toPath());

                // 상태코드 설정 (Enum 사용)
                response.setStatus(HttpStatus.OK);
                // 파일 확장자를 보고 Content-Type 결정 (HttpResponse 내부 로직)
                response.setContentType(resourcePath);
                // 최종 전송
                response.send(body);

                logger.debug("Response Sent: {} (Size: {})", resourcePath, body.length);
            } else {
                // 파일이 없을 경우 404 응답
                logger.error("File Not Found: {}", file.getAbsolutePath());
                response.setStatus(HttpStatus.NOT_FOUND);
                response.send("<h1>404 Not Found</h1>".getBytes());
            }

        } catch (IOException e) {
            logger.error("IO 에러 발생: {}", e.getMessage());
        }
    }

    // 경로 보정 헬퍼 메서드
    private String getResourcePath(String path) {
        if (isImageFile(path) && !path.contains("/img/") && !path.contains("favicon.ico")) {
            return "/img" + (path.startsWith("/") ? "" : "/") + path;
        }
        return path;
    }

    private boolean isImageFile(String path) {
        return path.endsWith(".png") || path.endsWith(".jpg") ||
                path.endsWith(".jpeg") || path.endsWith(".gif") ||
                path.endsWith(".svg") || path.endsWith(".ico");
    }
}