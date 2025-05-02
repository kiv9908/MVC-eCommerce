package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * HikariCP를 사용하여 데이터베이스 연결 풀을 관리하는 유틸리티 클래스
 */
@Slf4j
public class DatabaseConnection {
    private static HikariDataSource dataSource = null;
    private static final Properties properties = new Properties();

    static {
        try {
            // 클래스패스에서 db.properties 파일 로드
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream("properties/db.properties");

            if (input == null) {
                throw new IllegalStateException("properties/db.properties 파일을 찾을 수 없습니다.");
            }

            properties.load(input);
            input.close();

            log.info("DB 설정 로드 성공: " + properties.getProperty("db.url"));

            // Wallet 폴더 경로 탐색
            URL walletUrl = classLoader.getResource("properties/Wallet_DinkDB");

            if (walletUrl != null) {
                String walletPath = new File(walletUrl.toURI()).getAbsolutePath();
                log.info("oracle.net.tns_admin: "+ walletPath);
                System.setProperty("oracle.net.tns_admin", walletPath);
                log.info("TNS_ADMIN 환경 변수 설정: " + walletPath);
            } else {
                System.err.println("Wallet_DinkDB 폴더를 찾을 수 없습니다.");
            }

            // SSL 관련 설정
            System.setProperty("oracle.net.ssl_version", "1.2");
            System.setProperty("oracle.net.ssl_server_dn_match", "true");

            // HikariCP 설정
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.user"));
            config.setPassword(properties.getProperty("db.password"));
            config.setDriverClassName(properties.getProperty("db.driver"));

            // 커넥션 풀 설정
            config.setMaximumPoolSize(3); // 최대 커넥션 수
            config.setMinimumIdle(2); // 최소 유지 커넥션 수
            config.setIdleTimeout(30000); // 유휴 커넥션 타임아웃 (30초)
            config.setConnectionTimeout(30000); // 커넥션 획득 타임아웃 (30초)
            config.setPoolName("eCommercePool"); // 풀 이름 설정

            // 연결 속성 설정
            Properties connectionProps = new Properties();
            connectionProps.setProperty("characterEncoding", "UTF-8");
            connectionProps.setProperty("useUnicode", "true");
            config.setDataSourceProperties(connectionProps);

            // TNS_ADMIN 설정을 HikariCP 데이터소스 속성에 추가
            String tnsAdmin = System.getProperty("oracle.net.tns_admin");
            if (tnsAdmin != null) {
                config.addDataSourceProperty("oracle.net.tns_admin", tnsAdmin);
            }

            // HikariDataSource 생성
            dataSource = new HikariDataSource(config);

            log.info("HikariCP 커넥션 풀 초기화 성공");

        } catch (Exception e) {
            log.error("HikariCP 설정 중 오류 발생:", e);
            throw new RuntimeException("데이터베이스 풀 초기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 커넥션 풀에서 연결을 가져옵니다.
     * @return 데이터베이스 연결
     */
    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("데이터베이스 연결에 실패했습니다: " + e.getMessage(), e);
            throw new RuntimeException("데이터베이스 연결 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 풀을 닫고 모든 리소스를 해제합니다.
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP 커넥션 풀이 종료되었습니다.");
        }
    }

    /**
     * 트랜잭션 관리와 관련된 중요한 변경 사항:
     * HikariCP를 사용할 때는 Connection을 매번 반환해야 합니다.
     * 아래 메서드들은 여전히 필요하지만, 트랜잭션은 각 연결마다 개별적으로 관리됩니다.
     */

    /**
     * 트랜잭션을 커밋합니다.
     * @param connection 커밋할 연결
     * @throws SQLException SQL 예외 발생 시
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.commit();
            log.info("트랜잭션이 커밋되었습니다.");
        }
    }

    /**
     * 트랜잭션을 롤백합니다.
     * @param connection 롤백할 연결
     * @throws SQLException SQL 예외 발생 시
     */
    public static void rollbackTransaction(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.rollback();
            log.info("트랜잭션이 롤백되었습니다.");
        }
    }

    /**
     * 연결을 닫고 풀로 반환합니다.
     * @param connection 닫을 연결
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close(); // HikariCP에서는 이것이 풀로 반환하는 것입니다
                log.debug("연결이 풀로 반환되었습니다.");
            } catch (SQLException e) {
                log.error("연결 반환 중 오류: " + e.getMessage(), e);
            }
        }
    }
}