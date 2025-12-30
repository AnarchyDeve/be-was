package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 아래 런어블로 만드는 이유는 쓰레드를 한명 배치한다음에 무슨일을 해야하는지 만드는 명세서 라고 생각하면됌
public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class); // 로거 등록할꺼여서 그냥 또 써주는거임

    private Socket connection; // 그냥 소켓을 커넥션으로 함 근데 이게 웹서버에서 클라이언트임 맞아?
    // 리퀘스트핸들러 부분이 나중에 핸들러 맵핑이랑 컨트롤러로 바뀜 디스페치 서블릿이랑 같음
    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    } // 리퀘스트 핸들러는 이거는 요청에 대한 핸들러임 이게 컨트롤러에서 요청을 받는 거고 이걸로 인해서 나중에 핸들러 맵핑을 나중에 진행할꺼임
// 쓰레드는 thread.start() 를 하게 되면 런어블의 런을 실행하게 됌
    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        // 클라이언트 IP 와 포트 번호를 디버깅함
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // 인풋 스트림은 클라이언트가 행위 http 매서드로 해서 보내는것을 받아들이는 톨로고 아웃은 보낼 통로라고 생각하면됌
            // InputStream을 한 줄씩 읽기 편하게 BufferedReader로 감싸기
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();

            logger.debug("Request Line: {}", line); // 브라우저가 보낸 첫 번째 줄 출력

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            // 아웃스트림은 데이터를 바이트 형태로 보내야하는데 데이터아웃스트림을 쓰게 된다면 문자열 인트형 등 자동으로 변환해서 보내준다
            byte[] body = "<h1>Hello World</h1>".getBytes();
            // 바디에 바이트로 해서 헬로우 월드를 만든다.
            response200Header(dos, body.length);
            // 그리고 아웃 바이트 코드를 해더와 몸에 길이를 해더에 실어서 200 코드 정상 코드를 호출한다.
            responseBody(dos, body);
            // 그리고 본문에 아웃 통로와 바디값을 넘겨서 호출한다.
        } catch (IOException e) {
            logger.error(e.getMessage()); // 로거의 에러에 예외 e의 겟메시지로 나타낸다.
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
}
