package command.admin.user;

import command.Command;
import config.AppConfig;
import domain.dto.PageDTO;
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

        try {
            // 요청 파라미터에서 PageDTO 생성
            PageDTO pageDTO = userService.createPageDTOFromParameters(
                    request.getParameter("page")
            );

            // 페이지 크기 설정
            pageDTO.setPageSize(10);

            // 서비스 계층에서 페이지네이션 설정
            pageDTO = userService.setupUserPage(pageDTO);

            // 사용자 목록 조회
            List<UserDTO> pagedUsers = userService.getUsersWithPagination(
                    pageDTO.getCurrentPage(), pageDTO.getPageSize()
            );

            // 결과 저장
            request.setAttribute("users", pagedUsers);

            // PageDTO를 request에 저장
            request.setAttribute("pageDTO", pageDTO);

            return "/WEB-INF/views/admin/user/userList.jsp";
        } catch (Exception e) {
            log.error("회원 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}