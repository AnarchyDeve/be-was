package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();

            if (line == null) return;

            String[] tokens = line.split(" ");
            String path = tokens[1];

            // 1. 루트 경로 처리 예외 처리를 잡기위해 있는거임
            if (path.equals("/")) {
                path = "/index.html";
            }

            // 2. 헤더 읽기 (끝까지 읽어야 다음 요청에 문제가 없음)
            while (line != null && !line.equals("")) {
                line = br.readLine();
            }

            // 3. 경로 보정 로직 (static + img)
            String resourcePath = path;

            // 이미지 파일이면서, 이미 /img/ 경로가 없고, favicon이 아닐 때만 /img를 붙임
            if (isImageFile(path) && !path.contains("/img/") && !path.contains("favicon.ico")) {
                resourcePath = "/img" + (path.startsWith("/") ? "" : "/") + path;
            }

            // 4. 파일 읽기
            File file = new File("./src/main/resources/static" + resourcePath);

            if (!file.exists()) {
                logger.error("파일이 없습니다! 찾으려 했던 경로: {}", file.getAbsolutePath());
                return;
            }

            byte[] body = Files.readAllBytes(file.toPath());
            String contentType = getContentType(resourcePath);

            // 5. 응답 전송
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);

        } catch (IOException e) {
            logger.error("에러 발생: {}", e.getMessage());
        }
    }

    private boolean isImageFile(String path) {
        return path.endsWith(".png") || path.endsWith(".jpg") ||
                path.endsWith(".jpeg") || path.endsWith(".gif") ||
                path.endsWith(".svg") || path.endsWith(".ico");
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/html;charset=utf-8";
    }
}