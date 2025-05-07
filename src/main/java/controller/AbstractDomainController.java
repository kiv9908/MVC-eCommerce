package controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import command.Command;
import command.CommandFactory;

public abstract class AbstractDomainController extends HttpServlet {
    protected CommandFactory commandFactory;
    protected String domainPath; // 'user', 'admin' 등 도메인 경로

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String commandPath = requestURI.substring(contextPath.length());

        // /user/join, /admin/user 등의 형태에서 명령어 추출
        String command = "";
        if (commandPath.startsWith("/" + domainPath + "/")) {
            // 도메인 경로 이후의 부분을 명령어로 사용
            command = commandPath.substring(("/" + domainPath + "/").length());
        }

        // 명령어가 비어있거나 추가 경로가 있는 경우 처리 (/user/join/extra)
        if (command.isEmpty() || command.contains("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.");
            return;
        }

        try {
            Command cmd = commandFactory.getCommand(command);
            if (cmd == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.");
                return;
            }

            String viewPage = cmd.execute(request, response);

            if (viewPage != null) {
                if (viewPage.startsWith("redirect:")) {
                    response.sendRedirect(viewPage.substring(9));
                } else {
                    request.getRequestDispatcher(viewPage).forward(request, response);
                }
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}