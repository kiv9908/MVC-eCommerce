package config;

import domain.dao.*;
import lombok.Getter;
import service.*;
import util.DatabaseConnection;

import java.io.Serializable;

@Getter
public class AppConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // volatile 키워드 추가로 멀티스레드 환경에서 변수의 가시성 보장
    private static volatile AppConfig instance;

    private final UserDAO userDAO;
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final MappingDAO mappingDAO;
    private final ContentDAO contentDAO;

    private final UserService userService;
    private ProductService productService;
    private final AuthService authService;
    private final MappingService mappingService;
    private final CategoryService categoryService;

    // FileService 필드 추가
    private FileService fileService;


    /**
     * 생성자를 private으로 선언하여 외부에서 인스턴스 생성 방지
     */
    private AppConfig() {
        // 리포지토리 계층 초기화
        this.userDAO = new UserDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
        this.mappingDAO = new MappingDAOImpl();
        this.contentDAO = new ContentDAOImpl();

        // 서비스 계층 초기화 및 의존성 주입
        this.userService = new UserService(userDAO);
        this.authService = new AuthService(userDAO);
        this.mappingService = new MappingService(mappingDAO);
        this.categoryService = new CategoryService(categoryDAO);

        this.productService = null;
        this.fileService = null;
    }

    /**
     * Double-Checked Locking 방식으로 싱글톤 인스턴스 반환
     * 스레드 안전성 보장
     */
    public static AppConfig getInstance() {
        // 첫 번째 검사 (락 획득 전)
        if (instance == null) {
            // 락 획득
            synchronized (AppConfig.class) {
                // 두 번째 검사 (락 획득 후)
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    /**
     * 역직렬화 시 새 인스턴스 생성 방지
     */
    protected Object readResolve() {
        return getInstance();
    }

    public void initializeServices(String uploadPath, boolean useDbStorage) {
        // FileService 초기화
        this.fileService = new FileService(uploadPath, useDbStorage);

        // FileService에 의존하는 서비스 초기화
        this.productService = new ProductService(productDAO, mappingService, fileService);
    }

    public ProductService getProductService() {
        if (this.productService == null) {
            throw new IllegalStateException("ProductService가 초기화되지 않았습니다. initializeServices()를 먼저 호출해주세요.");
        }
        return this.productService;
    }

    /**
     * 애플리케이션 종료 시 자원 정리
     */
    public void closeResources() {
        // 애플리케이션 종료 시 HikariCP 풀 종료
        DatabaseConnection.closePool();
    }
}