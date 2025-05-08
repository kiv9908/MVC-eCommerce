package controller.admin;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import command.factory.admin.AdminCategoryCommandFactory;
import controller.AbstractDomainController;

@WebServlet(name = "AdminCategoryController", urlPatterns = "/admin/category/*")
public class AdminCategoryController extends AbstractDomainController {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.commandFactory = new AdminCategoryCommandFactory();
        this.domainPath = "admin/category";
    }
}