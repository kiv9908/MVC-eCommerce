package controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import command.Command;
import command.CommandFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDomainController extends HttpServlet {
    protected CommandFactory commandFactory;
    protected String domainPath; // 'user', 'admin' 등 도메인 경로

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String commandPath = requestURI.substring(contextPath.length());

        log.debug("요청 경로: {}", commandPath);

        // /user/join, /admin/user 등의 형태에서 명령어 추출
        String command = "";
        if (commandPath.startsWith("/" + domainPath + "/")) {
            // 도메인 경로 이후의 부분을 명령어로 사용
            String restPath = commandPath.substring(("/" + domainPath + "/").length());

            // /edit/123 형태일 경우 첫 번째 세그먼트만 명령어로 사용
            int slashIndex = restPath.indexOf('/', 1); // 첫 글자가 '/'인 경우를 처리하기 위해 1부터 시작
            if (slashIndex > 0) {
                command = restPath.substring(0, slashIndex);
            } else {
                command = restPath;
            }

            // 선행/후행 슬래시 제거
            command = command.replaceAll("^/+|/+$", "");
        }

        // 명령어가 비어있는 경우 처리
        if (command.isEmpty()) {
            // 기본 명령어를 'list'로 설정하거나 에러 반환
            command = "list"; // 또는 에러 페이지로 리다이렉트
        }

        log.debug("실행할 명령어: {}", command);

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
            log.error("명령 실행 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
}