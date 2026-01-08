package webserver;

import adapter.ControllerHandlerAdapter;
import adapter.HandlerAdapter;
import controller.HandlerMapping;
import controller.ResourceController;
import http.*;
import view.MyView;
import view.ViewResolver;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static webserver.WebServer.logger;

public class DispatcherServlet implements Runnable {
    private Socket connection;
    private final List<HandlerAdapter> handlerAdapters = new ArrayList<>();

    public DispatcherServlet(Socket connectionSocket) {
        this.connection = connectionSocket;
        handlerAdapters.add(new ControllerHandlerAdapter());
    }

    @Override
    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            // 1. 요청/응답 객체 생성 (이때 HttpRequest 내부에서 쿠키가 파싱됨)
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            // 2. [세션 연결 및 로그 기록]
            String sid = request.getCookie("sid");
            if (sid != null) {
                HttpSession session = HttpSessions.getSession(sid);

                if (session != null) {
                    // [정상] 세션이 존재함
                    logger.info(" [세션 확인] Path: {} | SID: {} | User: {}",
                            request.getPath(), sid, session.getAttribute("user"));

                    request.setSession(session);
                    session.access();
                } else {
                    // [유실/만료] 쿠키는 왔는데 서버에 데이터가 없음
                    logger.warn(" [세션 유실] 만료된 SID 발견: {}. 브라우저 쿠키를 초기화합니다.", sid);

                    // ★ 핵심: 브라우저가 더 이상 잘못된 sid를 보내지 않도록 쿠키 삭제 응답을 보냄
                    response.addHeader("Set-Cookie", "sid=; Path=/; Max-Age=0");
                }
            } else {
                // [최초/미인증] 쿠키 자체가 없음
                logger.debug(" [미인증 요청] 세션 쿠키 없음 | Path: {}", request.getPath());
            }

            // 3. 핸들러 매핑 및 실행
            Object handler = HandlerMapping.getHandler(request.getPath());
            if (handler == null) {
                handler = new ResourceController(); // 매핑 실패 시 정적 리소스(HTML, CSS 등) 처리
            }

            HandlerAdapter adapter = getHandlerAdapter(handler);
            String viewName = adapter.handle(request, response, handler);

            // ★ 바로 이 부분입니다! 컨트롤러가 직접 응답하면 viewName은 null이 됩니다.
            // null인 경우 "아, 할 일 다 끝났구나" 하고 바로 퇴근(return)시킵니다.
            if (viewName == null) {
                return;
            }
            // 4. 응답 처리 (Redirect vs View Rendering)
            if (viewName.startsWith("redirect:")) {
                String redirectPath = viewName.substring("redirect:".length());
                response.sendRedirect(HttpStatus.FOUND, redirectPath);
                return;
            }

            ViewResolver viewResolver = new ViewResolver();
            MyView view = viewResolver.resolve(viewName);
            view.render(request, response);

        } catch (Exception e) {
            logger.error(" DispatcherServlet 에러 발생: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private HandlerAdapter getHandlerAdapter(Object handler) {
        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        throw new IllegalArgumentException("핸들러 어댑터를 찾을 수 없습니다. handler=" + handler);
    }
}