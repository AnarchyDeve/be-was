package db;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserRepository {

    public static void addUser(User user) {
        //  SQL 예약어 충돌 방지를 위해 "USER" 테이블명 사용
        String sql = "INSERT INTO \"USER\" (userId, password, name, email, profileImage) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getProfileImage());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("회원 저장 실패: " + e.getMessage());
        }
    }

    public static User findUserById(String userId) {
        String sql = "SELECT * FROM \"USER\" WHERE userId = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("userId"),
                            rs.getString("password"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("profileImage")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("회원 조회 실패: " + e.getMessage());
        }
        return null;
    }

    public static Collection<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM \"USER\"";

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("userId"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("profileImage")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("전체 회원 조회 실패: " + e.getMessage());
        }
        return users;
    }
}