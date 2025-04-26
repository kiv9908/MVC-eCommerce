package config;

import domain.repository.ProductRepository;
import domain.repository.ProductRepositoryImpl;
import domain.repository.UserRepository;
import domain.repository.UserRepositoryImpl;
import lombok.Getter;
import lombok.Setter;
import service.AuthService;
import service.ProductService;
import service.UserService;
import util.DatabaseConnection;

import java.sql.Connection;

@Getter
@Setter
public class AppConfig {
    private static AppConfig instance;

    private Connection conn;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private UserService userService;
    private ProductService productService;
    private AuthService authService;

    private AppConfig() {
        initComponents();
    }

    /**
     * 싱글톤 인스턴스 반환
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * 모든 컴포넌트 초기화 및 의존성 주입
     */
    private void initComponents() {
        // 데이터베이스 연결
        this.conn = DatabaseConnection.getConnection();

        // 리포지토리 계층
        this.userRepository = new UserRepositoryImpl(conn);
        this.productRepository = new ProductRepositoryImpl(conn);

        // 서비스 계층
        this.userService = new UserService(userRepository);
        this.authService = new AuthService(userRepository);
        this.productService = new ProductService(productRepository);

    }

    public void closeResources() {
        DatabaseConnection.closeConnection();
    }
}
