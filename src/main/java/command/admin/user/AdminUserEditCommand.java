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

@Slf4j
public class AdminUserEditCommand implements Command {
    private UserService userService;

    public AdminUserEditCommand() {
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();

        if ("GET".equals(method)) {
            // GET 요청 처리 - 수정 페이지 표시
            return handleGetRequest(request, response);
        } else if ("POST".equals(method)) {
            // POST 요청 처리 - 회원 정보 수정
            return handlePostRequest(request, response);
        }

        // 지원하지 않는 HTTP 메소드
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return null;
    }

    private String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("회원 정보 수정 페이지 로드");

        String userId = request.getParameter("userId");

        if (userId == null || userId.trim().isEmpty()) {
            request.setAttribute("message", "유효하지 않은 사용자 ID입니다.");
            request.setAttribute("messageType", "error");

            // 사용자 목록으로 이동
            return "/admin/user/list";
        }

        // 사용자 정보 조회
        UserDTO userDTO = userService.getUserByEmail(userId);
        if (userDTO == null) {
            request.setAttribute("message", "해당 사용자를 찾을 수 없습니다.");
            request.setAttribute("messageType", "error");

            // 사용자 목록으로 이동
            return "/admin/user/list";
        }

        request.setAttribute("user", userDTO);
        return "/WEB-INF/views/admin/user/userEdit.jsp";
    }

    private String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("회원 정보 수정 처리");
        request.setCharacterEncoding("UTF-8");

        // 파라미터 추출
        String userId = request.getParameter("userId");

        if (userId == null || userId.trim().isEmpty()) {
            request.setAttribute("message", "필수 파라미터가 누락되었습니다.");
            request.setAttribute("messageType", "error");
            return "/admin/user/list";
        }

        // 사용자 조회
        UserDTO userDTO = userService.getUserByEmail(userId);
        if (userDTO == null) {
            request.setAttribute("message", "사용자를 찾을 수 없습니다.");
            request.setAttribute("messageType", "error");
            return "/admin/user/list";
        }

        try {
            // 회원 정보 수정
            String userName = request.getParameter("userName");
            String mobileNumber = request.getParameter("mobileNumber");
            String userType = request.getParameter("userType");
            String status = request.getParameter("status");

            if (userName != null && !userName.trim().isEmpty()) {
                userDTO.setUserName(userName);
            }

            if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
                userDTO.setMobileNumber(mobileNumber);
            }

            if (status != null) {
                userDTO.setStatus(status);
            }

            userService.modifyUser(userDTO);

            // 회원 권한 변경
            if (userType != null && (userType.equals("10") || userType.equals("20"))) {
                userService.changeUserRole(userId, userType);
            }

            request.setAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
            request.setAttribute("messageType", "success");

            // 사용자 목록으로 리다이렉트
            return "redirect:/admin/user/list";

        } catch (Exception e) {
            log.error("회원 정보 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("message", "처리 중 오류가 발생했습니다: " + e.getMessage());
            request.setAttribute("messageType", "error");

            // 사용자 조회해서 폼에 다시 보여주기
            request.setAttribute("user", userDTO);
            return "/WEB-INF/views/admin/user/userEdit.jsp";
        }
    }
}