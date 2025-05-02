package controller.user;

import config.AppConfig;
import domain.model.User;
import exception.InvalidPasswordException;
import lombok.extern.slf4j.Slf4j;
import service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@WebServlet(name = "ModifyOkServlet", urlPatterns = {"/user/modify"})
public class ModifyOk extends HttpServlet {
    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        showUserModifyForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        processUserModify(request, response);

    }

    // 회원정보 수정 폼 표시
    private void showUserModifyForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("회원정보 수정 폼 표시");

        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원정보 수정 페이지에 접근 시도");
            response.sendRedirect(request.getContextPath() + "/user/login");
            return;
        }

        // 회원정보 수정 페이지로 포워드
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/modify.jsp");
        dispatcher.forward(request, response);
    }

    // 회원정보 수정 처리
    private void processUserModify(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원정보 수정 시도");
            response.sendRedirect(request.getContextPath() + "/user/login");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        String userId = sessionUser.getUserId();

        // 요청 파라미터에서 수정할 정보 추출
        String userName = request.getParameter("userName");
        String mobileNumber = request.getParameter("mobileNumber");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");

        log.info("회원정보 수정 시도 - 사용자ID: {}", userId);

        try {
            // 사용자 정보 조회
            User user = userService.getUserByEmail(userId);
            if (user == null) {
                log.warn("회원정보 수정 실패 - 사용자를 찾을 수 없음: {}", userId);
                request.setAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/modify.jsp");
                dispatcher.forward(request, response);
                return;
            }

            // 기본 정보 업데이트
            user.setUserName(userName);
            user.setMobileNumber(mobileNumber);

            // 비밀번호 변경 처리
            if (currentPassword != null && !currentPassword.isEmpty() &&
                    newPassword != null && !newPassword.isEmpty()) {

                // 비밀번호 변경 시도
                userService.changePassword(userId, currentPassword, newPassword);
                log.info("비밀번호 변경 성공 - 사용자ID: {}", userId);
            } else if ((currentPassword != null && !currentPassword.isEmpty()) ||
                    (newPassword != null && !newPassword.isEmpty())) {
                // 현재 비밀번호나 새 비밀번호 중 하나만 입력한 경우
                log.warn("비밀번호 변경 실패 - 현재 비밀번호와 새 비밀번호를 모두 입력해야 함");
                request.setAttribute("errorMessage", "비밀번호를 변경하려면 현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/modify.jsp");
                dispatcher.forward(request, response);
                return;
            }

            // 사용자 정보 업데이트
            userService.modifyUser(user);

            // 세션 정보 업데이트
            session.setAttribute("user", user);

            log.info("회원정보 수정 성공 - 사용자ID: {}", userId);

            // 성공 메시지 설정 및 회원정보 수정 페이지로 포워드
            request.setAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);

        } catch (exception.AuthenticationException e) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: {}", e.getMessage());
            request.setAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
        } catch (InvalidPasswordException e) {
            log.warn("비밀번호 변경 실패 - 새 비밀번호 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("회원정보 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
        }
    }
}