package model;

import java.time.LocalDateTime;

public class Article {
    private Long id;
    private String writer;    // User의 userId와 매핑 (N:1)
    private String title;
    private String contents;
    private String imagePath;
    private int likeCount;
    private LocalDateTime createdAt;

    public Article(String writer, String title, String contents, String imagePath) {
        this.writer = writer;
        this.title = title;
        this.contents = contents;

        //  imagePath가 null이거나 비어있으면 기본 이미지 경로 설정
        // 브라우저에서 접근 가능한 웹 경로(/img/...)를 사용합니다.
        this.imagePath = (imagePath != null && !imagePath.isEmpty())
                ? imagePath
                : "/img/article/basic_article.svg";

        this.likeCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWriter() { return writer; }
    public String getTitle() { return title; }
    public String getContents() { return contents; }
    public String getImagePath() { return imagePath; } // 이 경로가 HTML의 <img src="...">에 들어감

    // setter가 필요한 경우에도 동일한 방어 로직을 적용하는 것이 좋습니다.
    public void setImagePath(String imagePath) {
        this.imagePath = (imagePath != null && !imagePath.isEmpty())
                ? imagePath
                : "/img/article/basic_article.svg";
    }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}