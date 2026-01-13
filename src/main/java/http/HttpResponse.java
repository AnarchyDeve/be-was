package http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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

    public void forward(String path){
        try{
            String staticPath = "./src/main/resources/static";
            File file = new File(staticPath + path);

            if(!file.exists()){
                logger.warn("파일이 존재하지 않습니다: {}", file.getAbsoluteFile());
                sendError(HttpStatus.NOT_FOUND, "요청하신 파일을 찾을 . 없습니다.");
                return;
            }

            byte[] body = Files.readAllBytes(file.toPath());

            setContentType(path);

            send(body);
        } catch (IOException e){
            logger.error("파일 읽기 실패: {}", e.getMessage());
            sendError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    public void sendError(HttpStatus httpStatus, String s) {
        setStatus(status);
        addHeader("Content-Type", "text/html;charset=utf-8");

        String errorHtml = "<html><body><h1>" + status.getCode() + " " + status.getMessage() + "</h1><p>" + s + "</p></body></html>";
        send(errorHtml.getBytes());
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

    /**
     * 특정 쿠키를 브라우저에서 삭제하도록 설정합니다.
     * @param cookieName 삭제할 쿠키 이름 (예: "sid")
     */
    public void expireCookie(String cookieName) {
        // Max-Age=0으로 설정하여 브라우저가 즉시 삭제하게 만듭니다.
        // Path=/ 를 설정해야 모든 경로에서 해당 쿠키가 삭제됩니다.
        addHeader("Set-Cookie", cookieName + "=; Path=/; Max-Age=0;");
    }
}