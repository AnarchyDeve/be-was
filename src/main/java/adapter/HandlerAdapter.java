package adapter;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public interface HandlerAdapter {
    boolean supports(Object handler);

    String handle(HttpRequest request, HttpResponse response, Object handler) throws IOException;
}
