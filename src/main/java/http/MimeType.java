package http;

public enum MimeType {
    HTML("html", "text/html;charset=utf-8"),
    CSS("css", "text/css"),
    JS("js", "application/javascript"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    SVG("svg", "image/svg+xml"),
    ICO("ico", "image/x-icon"),
    DEFAULT("", "application/octet-stream");

    private final String extension;
    private final String contentType;

    MimeType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }
}