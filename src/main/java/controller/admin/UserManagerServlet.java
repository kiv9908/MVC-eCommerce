package controller.admin;

import config.AppConfig;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@WebServlet(name = "AdminModifyUserServlet", urlPatterns = "/admin/modify")
public class UserManagerServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<User> users = userService.getAllUsers();
        request.setAttribute("users", users);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/userManage.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 파라미터 추출
        String userId = request.getParameter("userId");
        String action = request.getParameter("action");

        if (userId == null || userId.trim().isEmpty() || action == null || action.trim().isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"필수 파라미터가 누락되었습니다.\"}");
            return;
        }

        // 사용자 조회
        User user = userService.getUserByEmail(userId);
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"사용자를 찾을 수 없습니다.\"}");
            return;
        }

        boolean success = false;
        String message = "";

        try {
            switch (action) {
                case "updateInfo":
                    // 회원 정보 수정
                    String userName = request.getParameter("userName");
                    String mobileNumber = request.getParameter("mobileNumber");

                    if (userName != null && !userName.trim().isEmpty()) {
                        user.setUserName(userName);
                    }

                    if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
                        user.setMobileNumber(mobileNumber);
                    }

                    userService.modifyUser(user);
                    success = true;
                    message = "회원 정보가 성공적으로 수정되었습니다.";
                    break;

                case "updateRole":
                    // 회원 권한 변경
                    String userType = request.getParameter("userType");

                    if (userType != null && (userType.equals("10") || userType.equals("20"))) {
                        success = userService.changeUserRole(userId, userType);
                        message = success ? "회원 권한이 성공적으로 변경되었습니다." : "회원 권한 변경에 실패했습니다.";
                    } else {
                        message = "유효하지 않은 권한 코드입니다.";
                    }
                    break;

                case "updateStatus":
                    // 회원 상태 변경
                    String status = request.getParameter("status");

                    if (status != null && (status.equals("ST00") || status.equals("ST01") ||
                                          status.equals("ST02") || status.equals("ST03"))) {
                        user.setStatus(status);
                        userService.modifyUser(user);
                        
                        // 회원 상태가 정상(ST01)으로 변경될 경우, 권한도 일반 사용자(10)로 변경
                        if ("ST01".equals(status) && !"10".equals(user.getUserType()) && !"20".equals(user.getUserType())) {
                            userService.changeUserRole(userId, "10");
                            message = "회원 승인이 완료되었습니다. 상태와 권한이 변경되었습니다.";
                        } else {
                            message = "회원 상태가 성공적으로 변경되었습니다.";
                        }
                        success = true;
                    } else {
                        message = "유효하지 않은 상태 코드입니다.";
                    }
                    break;

                case "approveWithdrawal":
                    // 탈퇴 요청 승인 (ST03 -> ST02)
                    if (user.getStatus().equals("ST03")) {
                        user.setStatus("ST02");
                        userService.modifyUser(user);
                        success = true;
                        message = "회원 탈퇴 요청이 승인되었습니다.";
                    } else {
                        message = "탈퇴 요청 상태인 회원만 승인할 수 있습니다.";
                    }
                    break;

                default:
                    message = "지원하지 않는 작업입니다.";
                    break;
            }
        } catch (Exception e) {
            log.error("회원 정보 수정 중 오류 발생: {}", e.getMessage(), e);
            message = "처리 중 오류가 발생했습니다: " + e.getMessage();
        }

        // JSON 응답
        response.setContentType("application/json");
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + message + "\"}");
    }
}