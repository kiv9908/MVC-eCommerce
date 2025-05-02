package config;

import lombok.extern.slf4j.Slf4j;
import util.DatabaseConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
@Slf4j
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("애플리케이션 시작: HikariCP 초기화");
        // 애플리케이션 시작 시 HikariCP 풀 자동 초기화 (첫 연결 요청 시 초기화됨)
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("애플리케이션 종료: HikariCP 리소스 정리");
        // 애플리케이션 종료 시 HikariCP 풀 정리
        DatabaseConnection.closePool();
    }
}