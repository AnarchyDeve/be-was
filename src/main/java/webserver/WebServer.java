// 버전 1 쓰레드 풀에 인원수를 지정해두고 하는 방식

package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit; // 시간 단위를 위해 추가

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    // 1. 설정을 상수로 관리 (나중에 수정하기 편함)
    private static final int THREAD_POOL_SIZE = 200;

    public static void main(String args[]) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);

        // 2. 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // WebServer.java 내 수정 부분
//// 고정된 숫자가 아니라 '캐싱' 방식을 사용합니다.
//        ExecutorService executor = Executors.newCachedThreadPool();
//
//// 실행 방식은 동일합니다.
//        executor.execute(new RequestHandler(connection));

        // WebServer.java 내 수정 부분 (JDK 21 이상 필요)
// 풀을 만들지 않고, 작업마다 새로운 가상 스레드를 생성하는 전용 실행기를 씁니다.
//        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
//
//// 실행 방식은 동일하지만, 내부적으로는 훨씬 가벼운 일꾼이 생성됩니다.
//        executor.execute(new RequestHandler(connection));
//
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                // 3. 일꾼에게 작업 요청
                executor.execute(new DispatcherServlet(connection));
            }
        } catch (Exception e) {
            logger.error("서버 실행 중 에러 발생: {}", e.getMessage());
        } finally {
            // 4.  서버 종료 시 스레드 풀 안전하게 닫기
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}