package controller.admin;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import command.factory.admin.AdminUserCommandFactory;
import controller.AbstractDomainController;

@WebServlet(name = "AdminUserController", urlPatterns = "/admin/user/*")
public class AdminUserController extends AbstractDomainController {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.commandFactory = new AdminUserCommandFactory();
        this.domainPath = "admin/user";
    }
}