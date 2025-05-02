package config;

import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;
import domain.dao.UserDAO;
import domain.dao.UserDAOImpl;
import lombok.Getter;
import lombok.Setter;
import service.AuthService;
import service.ProductService;
import service.UserService;
import util.DatabaseConnection;

@Getter
@Setter
public class AppConfig {
    private static AppConfig instance;

    private UserDAO userDAO;
    private ProductDAO productDAO;
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
        // 리포지토리 계층 - Connection 주입 없이 초기화
        this.userDAO = new UserDAOImpl();
        this.productDAO = new ProductDAOImpl();

        // 서비스 계층
        this.userService = new UserService(userDAO);
        this.authService = new AuthService(userDAO);
        this.productService = new ProductService(productDAO);
    }

    public void closeResources() {
        // 애플리케이션 종료 시 HikariCP 풀 종료
        DatabaseConnection.closePool();
    }
}