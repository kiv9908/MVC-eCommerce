package controller.user;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import command.factory.user.UserOrderCommandFactory;
import controller.AbstractDomainController;

/**
 * 사용자 주문 관련 요청을 처리하는 Controller 클래스
 */
@WebServlet(name = "UserOrderController", urlPatterns = "/user/order/*")
public class UserOrderController extends AbstractDomainController {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.commandFactory = new UserOrderCommandFactory();
        this.domainPath = "user/order";
    }
}