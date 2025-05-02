package controller.user;

import config.AppConfig;
import domain.model.User;
import exception.DuplicateEmailException;
import exception.InvalidEmailException;
import exception.InvalidPasswordException;
import exception.UserWithdrawnException;
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
@WebServlet(name = "joinOkServlet", urlPatterns = "/user/join")
public class JoinOk extends HttpServlet {
    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // AppConfig를 통해 의존성 주입
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        showJoinForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        processJoin(request, response);
    }

    private void showJoinForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("회원가입 폼 표시");

        // 이미 로그인된 사용자인 경우 메인 페이지로 리다이렉트
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            log.debug("이미 로그인된 사용자의 회원가입 페이지 접근 시도");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 회원가입 페이지로 포워드
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
        dispatcher.forward(request, response);
    }

    private void processJoin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 요청 파라미터에서 회원가입 정보 추출
        String userName = request.getParameter("userName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String mobileNumber = request.getParameter("mobileNumber");

        // 입력값 로깅 (보안을 위해 비밀번호는 마스킹)
        log.info("회원가입 시도 - 이름: {}, 이메일: {}, 휴대폰: {}", userName, email, mobileNumber);

        try {
            // 사용자 객체 생성
            User user = new User();
            user.setUserId(email); // 이메일을 사용자 ID로 사용
            user.setUserName(userName);
            user.setPassword(password);
            user.setEmail(email);
            user.setMobileNumber(mobileNumber);
            user.setUserType("10");
            user.setStatus("ST00"); // 승인 대기 상태로 설정

            // 회원가입 처리
            userService.register(user);

            log.info("회원가입 성공 - 이메일: {}", email);

            // 회원가입 성공 후 로그인 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/user/login?registered=true");

        } catch (InvalidPasswordException e) {
            log.warn("회원가입 실패 - 비밀번호 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
            dispatcher.forward(request, response);
        } catch (InvalidEmailException e) {
            log.warn("회원가입 실패 - 이메일 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
            dispatcher.forward(request, response);
        } catch (DuplicateEmailException e) {
            log.warn("회원가입 실패 - 중복된 이메일: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
            dispatcher.forward(request, response);
        } catch (UserWithdrawnException e) {
            log.warn("회원가입 실패 - 탈퇴한 회원: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
            dispatcher.forward(request, response);
        } catch (RuntimeException e) {
            log.error("회원가입 처리 중 런타임 오류 발생: {}", e.getMessage(), e);

            // 오류 메시지를 좀 더 상세하게 처리
            String errorMessage = "회원가입 처리 중 시스템 오류가 발생했습니다.";
            if (e.getMessage() != null && e.getMessage().contains("데이터베이스")) {
                errorMessage = "데이터베이스 연결 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }

            request.setAttribute("errorMessage", errorMessage);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/join.jsp");
            dispatcher.forward(request, response);
        }
    }
}