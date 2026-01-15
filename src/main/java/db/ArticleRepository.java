package db;

import model.Article;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {

    public static Long save(Article article) {
        String sql = "INSERT INTO ARTICLE (writer, title, contents, imagePath, createdAt) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { //  ID 반환 옵션

            pstmt.setString(1, article.getWriter());
            pstmt.setString(2, article.getTitle());
            pstmt.setString(3, article.getContents());
            pstmt.setString(4, article.getImagePath());
            pstmt.setTimestamp(5, Timestamp.valueOf(article.getCreatedAt()));

            pstmt.executeUpdate();

            //  생성된 PK(ID) 가져오기
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) { throw new RuntimeException("저장 실패", e); }
        return null;
    }

    public static void updateImagePath(Long articleId, String imagePath) {
        String sql = "UPDATE ARTICLE SET imagePath = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, imagePath);
            pstmt.setLong(2, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("업데이트 실패", e); }
    }

    public static List<Article> findAll() {
        String sql = "SELECT * FROM ARTICLE ORDER BY createdAt DESC";
        List<Article> articles = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Article article = new Article(rs.getString("writer"), rs.getString("title"), rs.getString("contents"), rs.getString("imagePath"));
                article.setId(rs.getLong("id"));
                articles.add(article);
            }
        } catch (SQLException e) { throw new RuntimeException("조회 실패", e); }
        return articles;
    }
}