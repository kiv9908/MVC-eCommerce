package controller.user;

import config.AppConfig;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.AuthService;
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
@Slf4j
@WebServlet(name = "WithdrawServlet", urlPatterns = "/user/withdraw")
public class WithdrawServlet extends HttpServlet {
    private AuthService authService;
    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // AppConfig를 통해 의존성 주입
        AppConfig appConfig = AppConfig.getInstance();
        this.authService = appConfig.getAuthService();
        this.userService = appConfig.getUserService();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        showWithdrawForm(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processWithdraw(request, response);
    }

    // 회원 탈퇴 폼 표시
    private void showWithdrawForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("회원 탈퇴 폼 표시");

        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원 탈퇴 페이지에 접근 시도");
            response.sendRedirect(request.getContextPath() + "/user/login");
            return;
        }

        // 회원 탈퇴 페이지로 포워드
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/withdraw.jsp");
        dispatcher.forward(request, response);
    }

    // 회원 탈퇴 처리
    private void processWithdraw(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원 탈퇴 시도");
            response.sendRedirect(request.getContextPath() + "/user/login");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        String userId = sessionUser.getUserId();

        // 요청 파라미터에서 비밀번호 및 확인 체크박스 값 추출
        String password = request.getParameter("password");
        String confirmWithdraw = request.getParameter("confirmWithdraw");

        log.info("회원 탈퇴 시도 - 사용자ID: {}", userId);

        // 필수 파라미터 확인
        if (password == null || password.isEmpty()) {
            log.warn("회원 탈퇴 실패 - 비밀번호 미입력");
            request.setAttribute("errorMessage", "비밀번호를 입력해주세요.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/withdraw.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (confirmWithdraw == null || !confirmWithdraw.equals("yes")) {
            log.warn("회원 탈퇴 실패 - 동의 체크박스 미선택");
            request.setAttribute("errorMessage", "회원 탈퇴에 동의해주셔야 합니다.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/withdraw.jsp");
            dispatcher.forward(request, response);
            return;
        }

        try {
            // 비밀번호 확인을 위해 로그인 시도
            User user = authService.login(userId, password);

            // 회원 탈퇴 처리
            boolean success = userService.requestWithdrawal(userId);

            if (success) {
                log.info("회원 탈퇴 성공 - 사용자ID: {}", userId);

                // 세션 무효화
                session.invalidate();

                // 탈퇴 완료 메시지와 함께 로그인 페이지로 리다이렉트
                response.sendRedirect(request.getContextPath() + "/user/login?withdrawn=true");
            } else {
                log.warn("회원 탈퇴 실패 - 탈퇴 처리 중 오류 발생");
                request.setAttribute("errorMessage", "회원 탈퇴 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/withdraw.jsp");
                dispatcher.forward(request, response);
            }

        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage(), e);

            // 로그인 인증 실패인 경우
            if (e.getMessage().contains("비밀번호가 일치하지 않습니다")) {
                request.setAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            } else {
                request.setAttribute("errorMessage", "회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/withdraw.jsp");
            dispatcher.forward(request, response);
        }
    }
}