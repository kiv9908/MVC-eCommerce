package command.admin.user;

import command.Command;
import config.AppConfig;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class AdminUserListCommand implements Command {
    private UserService userService;

    public AdminUserListCommand() {
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("회원 목록 조회 실행");

        // 사용자 목록 조회
        List<UserDTO> users = userService.getAllUsers();
        request.setAttribute("users", users);

        return "/WEB-INF/views/admin/user/userList.jsp";
    }
}