package controller.admin;

import command.factory.admin.AdminOrderCommandFactory;
import controller.AbstractDomainController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;

/**
 * 관리자 주문 관리 컨트롤러
 * URL 패턴: /admin/order/*
 */
@WebServlet("/admin/order/*")
@Slf4j
public class AdminOrderController extends AbstractDomainController {

    public AdminOrderController() {
        // 도메인 경로 설정
        this.domainPath = "admin/order";
        // 명령어 팩토리 설정
        this.commandFactory = new AdminOrderCommandFactory();
    }
}