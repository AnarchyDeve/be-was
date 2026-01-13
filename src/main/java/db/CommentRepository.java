package db;

import model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {
    private static List<Comment> comments = new ArrayList<>();

    public static void addComment(Comment comment) {
        comments.add(comment);
    }

    public static List<Comment> findAll() {
        return comments;
    }
/*
    CREATE TABLE IF NOT EXISTS COMMENT (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            articleId BIGINT NOT NULL,          -- 어떤 게시글에 달린 댓글인지 (ARTICLE.id)
    writer VARCHAR(50) NOT NULL,        -- 누가 썼는지 (USERS.userId)
    contents TEXT NOT NULL,             -- 댓글 내용
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 외래키 설정 (데이터 무결성을 위해 권장)
    FOREIGN KEY (articleId) REFERENCES ARTICLE(id) ON DELETE CASCADE,
    FOREIGN KEY (writer) REFERENCES USERS(userId)
            );

 */
}
