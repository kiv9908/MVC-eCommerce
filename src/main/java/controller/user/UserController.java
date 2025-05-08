package controller.user;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import command.factory.UserCommandFactory;
import controller.AbstractDomainController;

@WebServlet(name = "UserController", urlPatterns = "/user/*")
public class UserController extends AbstractDomainController {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.commandFactory = new UserCommandFactory();
        this.domainPath = "user";
    }
}