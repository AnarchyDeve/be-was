package webserver;

import db.DatabaseInitializer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int THREAD_POOL_SIZE = 200;

    public static void main(String[] args) throws Exception {
        int port = (args == null || args.length == 0) ? DEFAULT_PORT : Integer.parseInt(args[0]);

        //  서버 시작 시 DB 초기화 (테이블이 없으면 만들고 있으면 유지)
        DatabaseInitializer.init();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                executor.execute(new DispatcherServlet(connection));
            }
        } catch (Exception e) {
            logger.error("서버 실행 에러: {}", e.getMessage());
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}