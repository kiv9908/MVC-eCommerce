package controller.user;

import command.factory.user.UserBasketCommandFactory;
import controller.AbstractDomainController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;

@Slf4j
@WebServlet("/user/basket.do/*")
public class UserBasketController extends AbstractDomainController {

    @Override
    public void init() {
        this.domainPath = "user/basket.do";
        this.commandFactory = new UserBasketCommandFactory();
        log.info("장바구니 컨트롤러 초기화 완료");
    }
}