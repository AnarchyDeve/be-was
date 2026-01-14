package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 데이터베이스 연결을 관리하는 클래스입니다.
 */
public class ConnectionManager {
    // H2 DB 연결 정보 (로컬 서버 모드 기준)
    // ~/test는 사용자 홈 디렉토리의 test.mv.db 파일을 의미합니다.
    private static final String URL = "jdbc:h2:tcp://localhost/~/test";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * 데이터베이스 연결 객체(Connection)를 반환합니다.
     * @return Connection 객체
     */
    public static Connection getConnection() {
        try {
            // H2 드라이버 로드
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC 드라이버를 찾을 수 없습니다. 라이브러리 설정을 확인하세요.", e);
        } catch (SQLException e) {
            throw new RuntimeException("데이터베이스 연결에 실패했습니다. H2 서버가 켜져 있는지 확인하세요.", e);
        }
    }
}