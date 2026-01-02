package adapter;

import http.HttpRequest;
import http.HttpResponse;

public interface HandlerAdapter {
    boolean supports(Object handler);

    String handle(HttpRequest request, HttpResponse response, Object handler);
}
