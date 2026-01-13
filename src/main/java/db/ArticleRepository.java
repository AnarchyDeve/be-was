package db;

import model.Article;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 데이터를 H2 데이터베이스와 연동하여 관리하는 클래스
 */
public class ArticleRepository {

    /**
     * 새로운 게시글을 데이터베이스에 저장합니다.
     * @param article 저장할 게시글 객체
     */
    public void save(Article article) {
        String sql = "INSERT INTO ARTICLE (writer, title, contents, imagePath, createdAt) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, article.getWriter());
            pstmt.setString(2, article.getTitle());
            pstmt.setString(3, article.getContents());
            pstmt.setString(4, article.getImagePath());
            pstmt.setTimestamp(5, Timestamp.valueOf(article.getCreatedAt()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("게시글 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 데이터베이스에 저장된 모든 게시글을 최신순으로 가져옵니다.
     * @return 게시글 리스트
     */
    public List<Article> findAll() {
        String sql = "SELECT * FROM ARTICLE ORDER BY createdAt DESC";
        List<Article> articles = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Article article = new Article(
                        rs.getString("writer"),
                        rs.getString("title"),
                        rs.getString("contents"),
                        rs.getString("imagePath")
                );
                article.setId(rs.getLong("id"));
                articles.add(article);
            }
        } catch (SQLException e) {
            throw new RuntimeException("게시글 목록 조회 중 오류가 발생했습니다.", e);
        }
        return articles;
    }
}

/*
위에가 실행되기 위해서는 아래꺼가 꼭 실행 되어야함.

CREATE TABLE IF NOT EXISTS ARTICLE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    writer VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    contents CLOB NOT NULL,
    imagePath VARCHAR(255),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 */