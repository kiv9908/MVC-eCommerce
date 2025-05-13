package controller.user;

import command.factory.user.UserProductCommandFactory;
import controller.AbstractDomainController;
import command.Command;
import command.CommandFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/user/product/*")
public class UserProductController extends AbstractDomainController {

    @Override
    public void init() throws ServletException {
        super.init();
        this.domainPath = "user/product";
        this.commandFactory = new UserProductCommandFactory();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.service(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.service(request, response);
    }
}