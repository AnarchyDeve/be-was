package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private RequestLine requestLine;
    private HttpCookies httpCookies;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map<String, byte[]> fileDatas = new HashMap<>(); //  추가: 파일 바이너리 보관
    private Map<String, String> fileNames = new HashMap<>(); //  추가: 원본 파일명 보관
    private HttpSession session;

    public HttpRequest(InputStream in) {
        try {
            String line = readLine(in);
            if (line == null || line.isEmpty()) return;

            this.requestLine = new RequestLine(line);
            this.params.putAll(requestLine.getParams());

            while (!(line = readLine(in)).equals("")) {
                String[] headerPair = line.split(":");
                if (headerPair.length >= 2) {
                    headers.put(headerPair[0].trim(), headerPair[1].trim());
                }
            }

            if (headers.containsKey("Cookie")) {
                this.httpCookies = new HttpCookies(headers.get("Cookie"));
            }

            String contentLengthStr = headers.get("Content-Length");
            if (contentLengthStr != null) {
                int contentLength = Integer.parseInt(contentLengthStr);
                String contentType = headers.get("Content-Type");

                if (contentType != null && contentType.contains("multipart/form-data")) {
                    parseMultipartBody(in, contentLength, contentType);
                } else {
                    byte[] bodyBytes = readRawBody(in, contentLength);
                    parseBodyParameters(new String(bodyBytes, StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            logger.error("Request 파싱 중 오류: {}", e.getMessage());
        }
    }

    private void parseMultipartBody(InputStream in, int contentLength, String contentType) throws IOException {
        String boundary = "--" + contentType.split("boundary=")[1];
        byte[] bodyBytes = readRawBody(in, contentLength);
        List<Integer> indexes = findBoundaryIndexes(bodyBytes, boundary.getBytes());

        for (int i = 0; i < indexes.size() - 1; i++) {
            int start = indexes.get(i) + boundary.getBytes().length + 2;
            int end = indexes.get(i + 1) - 2;
            if (start >= end) continue;
            byte[] partBytes = Arrays.copyOfRange(bodyBytes, start, end);
            parsePart(partBytes);
        }
    }

    private void parsePart(byte[] partBytes) {
        int headerEndIndex = findEmptyLineIndex(partBytes);
        String partHeader = new String(Arrays.copyOfRange(partBytes, 0, headerEndIndex), StandardCharsets.UTF_8);
        byte[] partBody = Arrays.copyOfRange(partBytes, headerEndIndex + 4, partBytes.length);

        String fieldName = extractAttribute(partHeader, "name");
        if (partHeader.contains("filename=")) {
            String fileName = extractAttribute(partHeader, "filename");
            if (fileName != null && !fileName.isEmpty()) {
                fileDatas.put(fieldName, partBody); //  메모리에 저장
                fileNames.put(fieldName, fileName);
            }
        } else {
            params.put(fieldName, new String(partBody, StandardCharsets.UTF_8).trim());
        }
    }

    // --- 헬퍼 및 유틸리티 메서드 (생략 없이 포함) ---
    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                int next = in.read();
                if (next == '\n') break;
                baos.write(b); baos.write(next);
            } else if (b == '\n') break;
            else baos.write(b);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    private byte[] readRawBody(InputStream in, int contentLength) throws IOException {
        byte[] data = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = in.read(data, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }
        return data;
    }

    private List<Integer> findBoundaryIndexes(byte[] body, byte[] boundary) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i <= body.length - boundary.length; i++) {
            boolean match = true;
            for (int j = 0; j < boundary.length; j++) {
                if (body[i + j] != boundary[j]) { match = false; break; }
            }
            if (match) indexes.add(i);
        }
        return indexes;
    }

    private int findEmptyLineIndex(byte[] part) {
        for (int i = 0; i < part.length - 3; i++) {
            if (part[i] == '\r' && part[i+1] == '\n' && part[i+2] == '\r' && part[i+3] == '\n') return i;
        }
        return -1;
    }

    private String extractAttribute(String header, String attribute) {
        String searchString = attribute + "=\"";
        int start = header.indexOf(searchString);
        if (start == -1) return null;
        start += searchString.length();
        int end = header.indexOf("\"", start);
        return (end == -1) ? null : header.substring(start, end);
    }

    private void parseBodyParameters(String bodyData) {
        if (bodyData == null || bodyData.isEmpty()) return;
        try {
            String decodedBody = java.net.URLDecoder.decode(bodyData, StandardCharsets.UTF_8.name());
            String[] pairs = decodedBody.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) params.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        } catch (UnsupportedEncodingException e) { logger.error("Body 파싱 오류: {}", e.getMessage()); }
    }

    public String getMethod() { return requestLine.getMethod(); }
    public String getPath() { return requestLine.getPath(); }
    public String getParameter(String name) { return params.get(name); }
    public byte[] getFileData(String fieldName) { return fileDatas.get(fieldName); } // 추가
    public String getFileName(String fieldName) { return fileNames.get(fieldName); } // 추가
    public HttpSession getSession() { return session; }
    public void setSession(HttpSession session) { this.session = session; }

    public String getCookie(String name) {
        // 1. 파싱된 쿠키 객체가 없으면 null 반환
        if (this.httpCookies == null) {
            return null;
        }

        // 2. HttpCookies 클래스에 구현된 getCookie 메서드를 호출하여 값을 가져옴
        return this.httpCookies.getCookie(name);
    }
}