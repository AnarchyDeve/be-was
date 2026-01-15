package model;

import java.time.LocalDateTime;

public class Comment {
    private Long id;              // 댓글 고유 번호
    private Long articleId;       //  연관된 게시글의 ID (Article과 N:1)
    private String userId;        //  작성자의 ID (User와 N:1)
    private String writerName;    // 화면 표시용 작성자 이름 (User의 name)
    private String contents;      // 댓글 본문
    private LocalDateTime createdAt;

    public Comment(Long articleId, String userId, String writerName, String contents) {
        this.articleId = articleId;
        this.userId = userId;
        this.writerName = writerName;
        this.contents = contents;
        this.createdAt = LocalDateTime.now();
    }

    // Getter 메서드
    public Long getId() { return id; }
    public Long getArticleId() { return articleId; }
    public String getUserId() { return userId; }
    public String getWriterName() { return writerName; }
    public String getContents() { return contents; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setter (Repository에서 자동 생성된 ID를 주입할 때 사용)
    public void setId(Long id) { this.id = id; }
}