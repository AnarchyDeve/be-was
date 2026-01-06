package adapter;

import controller.Controller;
import http.HttpRequest;
import http.HttpResponse;

public class ControllerHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return (handler instanceof Controller);
    }

    @Override
    public String handle(HttpRequest request, HttpResponse response, Object handler) {
        Controller controller = (Controller) handler;
        return controller.process(request, response);
    }
}
