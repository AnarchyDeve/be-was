package webserver;

import adapter.ControllerHandlerAdapter;
import adapter.HandlerAdapter;
import controller.HandlerMapping;
import controller.ResourceController;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSessions;
import http.HttpStatus;
import view.MyView;
import view.ViewResolver;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DispatcherServlet implements Runnable {
    private Socket connection;
    private final List<HandlerAdapter> handlerAdapters = new ArrayList<>();

    public DispatcherServlet(Socket connectionSocket) {
        this.connection = connectionSocket;
        // 사용할 어댑터들을 초기화 시 등록
        handlerAdapters.add(new ControllerHandlerAdapter());
    }

    @Override
    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpSessions.clearInvalidSessions();
            // 1. 요청/응답 객체 생성
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            // 2. HandlerMapping에서 담당 핸들러 찾기
            Object handler = HandlerMapping.getHandler(request.getPath());
            if (handler == null) {
                handler = new ResourceController(); // 매핑된 컨트롤러 없으면 정적 자원 핸들러로
            }

            // 3. 해당 핸들러를 실행할 수 있는 어댑터 찾기
            HandlerAdapter adapter = getHandlerAdapter(handler);

            // 4. 어댑터를 통해 핸들러 실행 및 뷰 이름(경로) 획득
            String viewName = adapter.handle(request, response, handler);
            if (viewName.startsWith("redirect:")) {
                // 1. "redirect:" 접두사를 제거하여 실제 이동할 주소 추출 (예: "/index.html")
                String redirectPath = viewName.substring("redirect:".length());

                // 2. HttpResponse 객체에 리다이렉트 명령 수행 (302 상태코드 전송)
                // 이 메서드는 내부적으로 response.sendRedirect() 같은 로직을 호출해야 합니다.
                response.sendRedirect(HttpStatus.FOUND,redirectPath);

                return; // 리다이렉트는 응답이 끝난 것이므로 아래 렌더링 로직을 타지 않게 종료
            }
            // 5. ViewResolver를 통해 MyView 객체 획득 (물리 경로 해결)
            ViewResolver viewResolver = new ViewResolver();
            MyView view = viewResolver.resolve(viewName);

            // 6. View 렌더링 (최종 응답 전송)
            view.render(request, response);

        } catch (Exception e) {
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