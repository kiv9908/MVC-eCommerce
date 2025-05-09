package command.user;

import command.Command;
import config.AppConfig;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCommand implements Command {
    private AuthService authService;

    public LoginCommand() {
        AppConfig appConfig = AppConfig.getInstance();
        this.authService = appConfig.getAuthService();
    }


    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.debug("로그인 폼 표시");

            // 이미 로그인된 사용자인 경우 메인 페이지로 리다이렉트
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                log.debug("이미 로그인된 사용자의 로그인 페이지 접근 시도");
                return "redirect:" + request.getContextPath() + "/";
            }

            // 로그인 페이지로 포워드
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            return "/WEB-INF/views/user/login.jsp";
        }

        // POST 요청 처리 - 로그인 로직 실행
        // 요청 파라미터에서 로그인 정보 추출
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 필수 파라미터 확인
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            log.warn("로그인 실패 - 이메일 또는 비밀번호가 입력되지 않음");
            request.setAttribute("errorMessage", "이메일과 비밀번호를 모두 입력해주세요.");
            return "/WEB-INF/views/user/login.jsp";
        }

        // 로그인 정보 로깅 (보안을 위해 비밀번호는 마스킹)
        log.info("로그인 시도 - 이메일: {}", email);

        try {
            // 로그인 시도
            UserDTO userDTO = authService.login(email, password);

            if (userDTO != null) {
                // 로그인 성공, 세션에 사용자 정보 저장
                HttpSession session = request.getSession();
                session.setAttribute("user", userDTO);
                session.setAttribute("isLoggedIn", true);

                // 관리자 여부 확인하여 세션에 저장
                boolean isAdmin = "20".equals(userDTO.getUserType());
                session.setAttribute("isAdmin", isAdmin);

                log.info("로그인 성공 - 사용자: {}, 관리자: {}", userDTO.getUserId(), isAdmin);

                // 원래 요청했던 URL이 있는지 확인
                String redirectURL = (String) session.getAttribute("redirectAfterLogin");

                if (redirectURL != null) {
                    // 세션에서 리다이렉트 URL 제거
                    session.removeAttribute("redirectAfterLogin");
                    return "redirect:"+redirectURL;
                } else {
                    // 기본 메인 페이지로 리다이렉트
                    return "redirect:" + request.getContextPath() + "/";
                }

            } else {
                // 로그인 실패 (이 부분은 실행되지 않을 수 있음, AuthService에서 예외가 발생할 수 있음)
                log.warn("로그인 실패 - 이메일: {}", email);
                request.setAttribute("errorMessage", "이메일 또는 비밀번호가 잘못되었습니다.");
                return "/WEB-INF/views/user/login.jsp";
            }

        } catch (Exception e) {
            // 예외 발생 (사용자를 찾을 수 없거나, 비밀번호가 일치하지 않거나, 계정이 비활성화된 경우 등)
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", e.getMessage());
            return "/WEB-INF/views/user/login.jsp";
        }
    }
}
