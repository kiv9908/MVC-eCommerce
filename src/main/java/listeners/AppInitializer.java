package listeners;

import config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@Slf4j
@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String uploadPath = context.getRealPath("/uploads");

        log.info("웹 애플리케이션 초기화 중...");
        log.info("업로드 경로: {}", uploadPath);

        // 2단계 초기화 호출
        AppConfig appConfig = AppConfig.getInstance();
        appConfig.initializeServices(uploadPath, true);

        log.info("서비스 초기화 완료");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("웹 애플리케이션 종료 중...");
        // 필요한 정리 작업
        AppConfig.getInstance().closeResources();
        log.info("자원 정리 완료");
    }
}
