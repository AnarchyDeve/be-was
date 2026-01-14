package model;

import java.time.LocalDateTime;

public class Article {
    private Long id;
    private String writer;
    private String title;
    private String contents;
    private String imagePath;
    private LocalDateTime createdAt;

    public Article(String writer, String title, String contents, String imagePath){
        this.writer =writer;
        this.title = title;
        this.contents = contents;
        this.imagePath = imagePath;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public String getWriter() {return writer;}
    public String getTitle() {return title;}
    public String getContents() {return contents;}
    public String getImagePath() {return imagePath;}
    public LocalDateTime getCreatedAt() {return  createdAt;}

    public void setId(Long id) {this.id= id;}

}
