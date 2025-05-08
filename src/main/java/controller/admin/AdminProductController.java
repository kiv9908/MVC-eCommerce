package controller.admin;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;

import command.factory.AdminProductCommandFactory;
import controller.AbstractDomainController;

@WebServlet(name = "AdminProductController", urlPatterns = "/admin/product/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1MB
        maxFileSize = 1024 * 1024 * 10,  // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class AdminProductController extends AbstractDomainController {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.commandFactory = new AdminProductCommandFactory();
        this.domainPath = "admin/product";
    }
}