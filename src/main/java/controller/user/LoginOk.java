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
@WebServlet(name = "loginOkServlet", urlPatterns = "/user/login")
public class LoginOk extends HttpServlet {
    private AuthService authService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // AppConfig를 통해 의존성 주입
        AppConfig appConfig = AppConfig.getInstance();
        this.authService = appConfig.getAuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        showLoginForm(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        processLogin(request, response);
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("로그인 폼 표시");

        // 이미 로그인된 사용자인 경우 메인 페이지로 리다이렉트
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            log.debug("이미 로그인된 사용자의 로그인 페이지 접근 시도");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 로그인 페이지로 포워드
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/login.jsp");
        dispatcher.forward(request, response);
    }

    private void processLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 요청 파라미터에서 로그인 정보 추출
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 필수 파라미터 확인
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            log.warn("로그인 실패 - 이메일 또는 비밀번호가 입력되지 않음");
            request.setAttribute("errorMessage", "이메일과 비밀번호를 모두 입력해주세요.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/login.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // 로그인 정보 로깅 (보안을 위해 비밀번호는 마스킹)
        log.info("로그인 시도 - 이메일: {}", email);

        try {
            // 로그인 시도
            User user = authService.login(email, password);

            if (user != null) {
                // 로그인 성공, 세션에 사용자 정보 저장
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("isLoggedIn", true);

                // 관리자 여부 확인하여 세션에 저장
                boolean isAdmin = "20".equals(user.getUserType());
                session.setAttribute("isAdmin", isAdmin);

                log.info("로그인 성공 - 사용자: {}, 관리자: {}", user.getUserId(), isAdmin);

                // 로그인 성공 후 메인 페이지 또는 대시보드로 리다이렉트
//                if (isAdmin) {
//                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
//                } else {
                    response.sendRedirect(request.getContextPath() + "/");
//                }
            } else {
                // 로그인 실패 (이 부분은 실행되지 않을 수 있음, AuthService에서 예외가 발생할 수 있음)
                log.warn("로그인 실패 - 이메일: {}", email);
                request.setAttribute("errorMessage", "이메일 또는 비밀번호가 잘못되었습니다.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/login.jsp");
                dispatcher.forward(request, response);
            }

        } catch (Exception e) {
            // 예외 발생 (사용자를 찾을 수 없거나, 비밀번호가 일치하지 않거나, 계정이 비활성화된 경우 등)
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/login.jsp");
            dispatcher.forward(request, response);
        }
    }
}