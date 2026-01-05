package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private DataOutputStream dos;
    private HttpStatus status;
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    // 1. 상태 코드 설정 (Enum 사용)
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    // 2. 헤더 추가
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    // 3. Content-Type 자동 설정 로직 (과거 방식으로 MimeType Enum 순회)
    public void setContentType(String path) {
        String extension = getFileExtension(path);
        String contentType = MimeType.DEFAULT.getContentType();

        for (MimeType mime : MimeType.values()) {
            if (mime.getExtension().equals(extension)) {
                contentType = mime.getContentType();
                break;
            }
        }
        addHeader("Content-Type", contentType);
    }

    // 4. 진짜로 브라우저에 전송하는 메서드
    public void send(byte[] body) {
        try {
            // [Step 1] Status Line 전송: HTTP/1.1 200 OK
            dos.writeBytes("HTTP/1.1 " + status.getCode() + " " + status.getMessage() + "\r\n");

            // [Step 2] Headers 전송
            for (String key : headers.keySet()) {
                dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
            }

            // 바디가 있다면 Content-Length는 필수
            if (body != null) {
                dos.writeBytes("Content-Length: " + body.length + "\r\n");
            }

            // [Step 3] 빈 줄 전송 (헤더의 끝 알림)
            dos.writeBytes("\r\n");

            // [Step 4] Body 전송
            if (body != null) {
                dos.write(body, 0, body.length);
            }

            dos.flush();
        } catch (IOException e) {
            logger.error("응답 전송 중 에러 발생: {}", e.getMessage());
        }
    }

    // 확장자 추출 헬퍼 메서드
    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1) return "";
        return path.substring(lastDotIndex + 1).toLowerCase();
    }
    public void sendRedirect(String path) {
        try {
            // 1. 상태 코드를 302 Found로 설정
            // HTTP 응답 라인: HTTP/1.1 302 Found
            dos.writeBytes("HTTP/1.1 302 Found \r\n");

            // 2. Header에 "Location" 추가
            // 브라우저에게 어디로 다시 접속할지 알려주는 핵심 헤더입니다.
            dos.writeBytes("Location: " + path + "\r\n");

            // 3. 응답 헤더의 끝을 알리는 빈 줄 전송
            dos.writeBytes("\r\n");

            // 4. 즉시 전송을 위해 플러시
            dos.flush();

            logger.debug("리다이렉트 응답 전송 완료: {}", path);
        } catch (IOException e) {
            logger.error("리다이렉트 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}