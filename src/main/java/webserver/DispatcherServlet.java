package webserver;

import adapter.ControllerHandlerAdapter;
import adapter.HandlerAdapter;
import controller.HandlerMapping;
import controller.ResourceController;
import http.HttpRequest;
import http.HttpResponse;
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