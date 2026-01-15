package db;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserRepository {

    /**
     * 회원가입: 새로운 유저 정보를 DB에 저장합니다.
     */
    public static void addUser(User user) {
        // "USER"는 H2 예약어이므로 쌍따옴표로 감쌉니다.
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
            throw new RuntimeException("회원 가입 저장 실패", e);
        }
    }

    /**
     * 로그인/조회: 아이디로 유저 정보를 찾습니다.
     */
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
            throw new RuntimeException("회원 조회 중 오류 발생", e);
        }
        return null;
    }

    /**
     * 전체 조회: 등록된 모든 유저 리스트를 가져옵니다.
     */
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
            throw new RuntimeException("전체 회원 목록 조회 실패", e);
        }
        return users;
    }

    /**
     * 마이페이지 수정: 회원 정보를 전체적으로 업데이트합니다.
     */
    public static void updateUserProfile(String userId, String name, String password, String profileImage) {
        // 💡 SQL UPDATE 문으로 DB 레코드를 직접 수정
        String sql = "UPDATE \"USER\" SET name = ?, password = ?, profileImage = ? WHERE userId = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, profileImage);
            pstmt.setString(4, userId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB 업데이트 중 오류 발생", e);
        }
    }
}