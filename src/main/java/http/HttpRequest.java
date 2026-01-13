package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private RequestLine requestLine;
    private HttpCookies httpCookies;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> saveFilePaths = new HashMap<>();
    private HttpSession session;

    public HttpRequest(InputStream in) {
        try {
            // 1. 헤더 파싱 (바이너리 데이터를 위해 직접 한 줄씩 읽기 구현)
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

            // 쿠키 처리
            if (headers.containsKey("Cookie")) {
                this.httpCookies = new HttpCookies(headers.get("Cookie"));
            }

            // 2. 바디 처리
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

    // InputStream에서 한 줄씩 읽는 헬퍼 메서드
    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                int next = in.read();
                if (next == '\n') break;
                baos.write(b);
                baos.write(next);
            } else if (b == '\n') {
                break;
            } else {
                baos.write(b);
            }
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

    private void parseMultipartBody(InputStream in, int contentLength, String contentType) throws IOException {
        String boundary = "--" + contentType.split("boundary=")[1];
        byte[] bodyBytes = readRawBody(in, contentLength);

        // boundary 위치 찾기 및 분할 로직
        List<Integer> indexes = findBoundaryIndexes(bodyBytes, boundary.getBytes());

        for (int i = 0; i < indexes.size() - 1; i++) {
            int start = indexes.get(i) + boundary.getBytes().length + 2; // \r\n 건너뛰기
            int end = indexes.get(i + 1) - 2; // \r\n 앞까지

            if (start >= end) continue;
            byte[] partBytes = Arrays.copyOfRange(bodyBytes, start, end);
            parsePart(partBytes);
        }
    }

    private void parsePart(byte[] partBytes) throws IOException {
        // Part 내의 헤더와 바디 분리 (\r\n\r\n 기준)
        int headerEndIndex = findEmptyLineIndex(partBytes);
        String partHeader = new String(Arrays.copyOfRange(partBytes, 0, headerEndIndex), StandardCharsets.UTF_8);
        byte[] partBody = Arrays.copyOfRange(partBytes, headerEndIndex + 4, partBytes.length);

        if (partHeader.contains("filename=")) {
            // 파일 파트
            String fieldName = extractAttribute(partHeader, "name");
            String fileName = extractAttribute(partHeader, "filename");
            saveFile(fieldName, fileName, partBody);
        } else {
            // 일반 텍스트 파트
            String fieldName = extractAttribute(partHeader, "name");
            params.put(fieldName, new String(partBody, StandardCharsets.UTF_8).trim());
        }
    }

    private void saveFile(String fieldName, String fileName, byte[] fileData) throws IOException {
        String uploadPath = "./src/main/resources/static/upload";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String savedName = System.currentTimeMillis() + "_" + fileName;
        Files.write(new File(uploadDir, savedName).toPath(), fileData);
        saveFilePaths.put(fieldName, "/upload/" + savedName);
        logger.debug("파일 저장: {} -> {}", fieldName, savedName);
    }

    // --- 유틸리티 메서드들 ---
    private List<Integer> findBoundaryIndexes(byte[] body, byte[] boundary) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i <= body.length - boundary.length; i++) {
            boolean match = true;
            for (int j = 0; j < boundary.length; j++) {
                if (body[i + j] != boundary[j]) {
                    match = false;
                    break;
                }
            }
            if (match) indexes.add(i);
        }
        return indexes;
    }

    private int findEmptyLineIndex(byte[] part) {
        for (int i = 0; i < part.length - 3; i++) {
            if (part[i] == '\r' && part[i+1] == '\n' && part[i+2] == '\r' && part[i+3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private String extractAttribute(String header, String attribute) {
        int start = header.indexOf(attribute + "=\"") + attribute.length() + 2;
        int end = header.indexOf("\"", start);
        return header.substring(start, end);
    }

    private void parseBodyParameters(String bodyData) {
        if (bodyData == null || bodyData.isEmpty()) return;

        try {
            // 1. 먼저 URL 디코딩을 수행하여 한글 및 특수문자를 복원합니다.
            // 브라우저는 폼 데이터를 보낼 때 기본적으로 application/x-www-form-urlencoded 방식을 씁니다.
            String decodedBody = java.net.URLDecoder.decode(bodyData, StandardCharsets.UTF_8.name());

            String[] pairs = decodedBody.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("="); // 첫 번째 '=' 위치를 찾음
                if (idx > 0) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    params.put(key, value);
                }
            }
            logger.debug("Body 파라미터 파싱 완료: {}", params);
        } catch (UnsupportedEncodingException e) {
            logger.error("Body 파싱 중 인코딩 오류: {}", e.getMessage());
        }
    }

    // --- Getter ---
    public String getMethod() { return requestLine.getMethod(); }
    public String getPath() { return requestLine.getPath(); }
    public String getParameter(String name) { return params.get(name); }
    public String getSaveFilePath(String fieldName) { return saveFilePaths.get(fieldName); }
    public HttpSession getSession() { return session; }
    public void setSession(HttpSession session) { this.session = session; }

    public String getCookie(String name) {
        if (httpCookies == null) {
            return null;
        }
        return httpCookies.getCookie(name);
    }
}