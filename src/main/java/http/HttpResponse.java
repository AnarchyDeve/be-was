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
    private HttpStatus status = HttpStatus.OK; // 기본값 200 설정
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    // [추가] 컨트롤러에서 가공된 바디(HTML 문자열 등)를 직접 보낼 때 사용
    public void forwardBody(byte[] body) {
        setStatus(HttpStatus.OK);
        addHeader("Content-Type", "text/html;charset=utf-8"); // HTML 전용 설정
        send(body);
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

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

    public void send(byte[] body) {
        try {
            // [Step 1] Status Line
            dos.writeBytes("HTTP/1.1 " + status.getCode() + " " + status.getMessage() + "\r\n");

            // [Step 2] Headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
            }

            if (body != null) {
                dos.writeBytes("Content-Length: " + body.length + "\r\n");
            }

            // [Step 3] Empty Line
            dos.writeBytes("\r\n");

            // [Step 4] Body
            if (body != null) {
                dos.write(body, 0, body.length);
            }

            dos.flush();
        } catch (IOException e) {
            logger.error("응답 전송 중 에러 발생: {}", e.getMessage());
        }
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1) return "";
        return path.substring(lastDotIndex + 1).toLowerCase();
    }

    public void sendRedirect(HttpStatus found, String redirectPath) {
        setStatus(found);
        addHeader("Location", redirectPath);
        send(null);
    }
}