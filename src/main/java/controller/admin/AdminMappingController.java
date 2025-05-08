package controller.admin;

import command.Command;
import command.factory.AdminCategoryCommandFactory;
import command.factory.AdminMappingCommandFactory;
import controller.AbstractDomainController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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