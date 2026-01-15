package db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void init() {
        // ClassLoader를 통해 리소스 폴더의 schema.sql을 읽어옴
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql")) {

            if (is == null) {
                logger.error(" 'src/main/resources/schema.sql' 파일을 찾을 수 없습니다.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                 Connection conn = ConnectionManager.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 전체 SQL 내용을 읽기
                String sql = reader.lines().collect(Collectors.joining("\n"));

                // 세미콜론(;) 단위로 쿼리를 분리하여 개별 실행
                String[] queries = sql.split(";");
                for (String query : queries) {
                    if (query != null && !query.trim().isEmpty()) {
                        stmt.execute(query.trim());
                    }
                }
                logger.info(" 데이터베이스 스키마 확인 및 생성 완료 (데이터 유지 모드)");
            }
        } catch (Exception e) {
            logger.error(" 데이터베이스 초기화 중 에러 발생: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}