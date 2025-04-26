package util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 데이터베이스 연결을 관리하는 유틸리티 클래스
 */
@Slf4j
public class DatabaseConnection {
    private static Connection connection = null;
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

            // JDBC 드라이버 로드
            Class.forName(properties.getProperty("db.driver"));
            log.info("JDBC 드라이버 로드 성공: " + properties.getProperty("db.driver"));

            // Wallet 폴더 경로 탐색
            URL walletUrl = classLoader.getResource("properties/Wallet_DinkDB");

            if (walletUrl != null) {
                String walletPath = new File(walletUrl.toURI()).getAbsolutePath();
                log.info("oracle.net.tns_admin: "+ walletPath);
                log.info("TNS_ADMIN 환경 변수 설정: " + walletPath);
            } else {
                System.err.println("Wallet_DinkDB 폴더를 찾을 수 없습니다.");
            }

            // SSL 관련 설정
            System.setProperty("oracle.net.ssl_version", "1.2");
            System.setProperty("oracle.net.ssl_server_dn_match", "true");

        } catch (Exception e) {
            log.info("Wallet 폴더 설정 중 오류 발생:");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // 데이터베이스에 연결
                log.info("DB 연결 시도: " + properties.getProperty("db.url"));
                log.info("사용자: " + properties.getProperty("db.user"));

                // 연결 속성 설정
                Properties connectionProps = new Properties();
                connectionProps.setProperty("user", properties.getProperty("db.user"));
                connectionProps.setProperty("password", properties.getProperty("db.password"));
                connectionProps.setProperty("characterEncoding", "UTF-8");
                connectionProps.setProperty("useUnicode", "true");

                // TNS_ADMIN 경로 설정
                String tnsAdmin = System.getProperty("oracle.net.tns_admin");
                if (tnsAdmin != null) {
                    connectionProps.setProperty("oracle.net.tns_admin", tnsAdmin);
                }

                // 연결 시도
                connection = DriverManager.getConnection(
                        properties.getProperty("db.url"),
                        connectionProps
                );

                // 자동 커밋 활성화
                connection.setAutoCommit(true);

                System.out.println("데이터베이스 연결 성공!");

            } catch (SQLException e) {
                log.info("데이터베이스 연결에 실패했습니다: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("데이터베이스 연결 실패: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    /**
     * 데이터베이스 연결을 닫습니다.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                log.info("데이터베이스 연결이 닫혔습니다.");
            } catch (SQLException e) {
                log.info("데이터베이스 연결 종료에 실패했습니다: " + e.getMessage());
            }
        }
    }

    /**
     * 트랜잭션을 커밋합니다.
     * @throws SQLException SQL 예외 발생 시
     */
    public static void commitTransaction() throws SQLException {
        if (connection != null) {
            connection.commit();
            log.info("트랜잭션이 커밋되었습니다.");
        }
    }

    /**
     * 트랜잭션을 롤백합니다.
     * @throws SQLException SQL 예외 발생 시
     */
    public static void rollbackTransaction() throws SQLException {
        if (connection != null) {
            connection.rollback();
            log.info("트랜잭션이 롤백되었습니다.");
        }
    }
}
