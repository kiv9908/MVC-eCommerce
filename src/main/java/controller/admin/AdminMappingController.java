package controller.admin;

import command.factory.admin.AdminMappingCommandFactory;
import controller.AbstractDomainController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@Slf4j
@WebServlet(name = "AdminMappingController", urlPatterns = "/admin/mapping/*")
public class AdminMappingController extends AbstractDomainController {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.commandFactory = new AdminMappingCommandFactory();
        this.domainPath = "admin/mapping";
    }
}