package command.user;

import command.Command;
import config.AppConfig;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.AuthService;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class WithdrawCommand implements Command {
    private AuthService authService;
    private UserService userService;

    public WithdrawCommand() {
        AppConfig appConfig = AppConfig.getInstance();
        this.authService = appConfig.getAuthService();
        this.userService = appConfig.getUserService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET 요청 처리 - 회원 탈퇴 폼 표시
        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.debug("회원 탈퇴 폼 표시");

            // 로그인 여부 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                log.warn("로그인되지 않은 사용자가 회원 탈퇴 페이지에 접근 시도");
                return "redirect:" + request.getContextPath() + "/user/login";
            }

            // 회원 탈퇴 페이지로 포워드
            return "/WEB-INF/views/user/withdraw.jsp";
        }

        // POST 요청 처리 - 회원 탈퇴 로직 실행
        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원 탈퇴 시도");
            return "redirect:" + request.getContextPath() + "/user/login";
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
            return "/WEB-INF/views/user/withdraw.jsp";
        }

        if (confirmWithdraw == null || !confirmWithdraw.equals("yes")) {
            log.warn("회원 탈퇴 실패 - 동의 체크박스 미선택");
            request.setAttribute("errorMessage", "회원 탈퇴에 동의해주셔야 합니다.");
            return "/WEB-INF/views/user/withdraw.jsp";
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
                return "redirect:" + request.getContextPath() + "/user/login?withdrawn=true";
            } else {
                log.warn("회원 탈퇴 실패 - 탈퇴 처리 중 오류 발생");
                request.setAttribute("errorMessage", "회원 탈퇴 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
                return "/WEB-INF/views/user/withdraw.jsp";
            }

        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage(), e);

            // 로그인 인증 실패인 경우
            if (e.getMessage().contains("비밀번호가 일치하지 않습니다")) {
                request.setAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            } else {
                request.setAttribute("errorMessage", "회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
            }

            return "/WEB-INF/views/user/withdraw.jsp";
        }
    }
}