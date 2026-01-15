package db;

import model.Comment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommentRepository {
    private static List<Comment> comments = new ArrayList<>();
    private static long sequence = 0L; // ID 생성을 위한 시퀀스

    public static void addComment(Comment comment) {
        comment.setId(++sequence); // 댓글 저장 시 고유 ID 부여
        comments.add(comment);
    }

    public static List<Comment> findAll() {
        return new ArrayList<>(comments);
    }

    /**
     * 특정 게시글(Article)의 ID와 일치하는 댓글 리스트만 반환합니다.
     * SQL의 'SELECT * FROM COMMENT WHERE articleId = ?'와 동일한 역할입니다.
     */
    public static List<Comment> findByArticleId(Long articleId) {
        return comments.stream()
                .filter(comment -> comment.getArticleId().equals(articleId))
                .collect(Collectors.toList());
    }
}