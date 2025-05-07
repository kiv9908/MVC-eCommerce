package command.user;

import command.Command;
import config.AppConfig;
import domain.model.User;
import exception.DuplicateEmailException;
import exception.InvalidEmailException;
import exception.InvalidPasswordException;
import exception.UserWithdrawnException;
import lombok.extern.slf4j.Slf4j;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
@Slf4j
public class JoinCommand implements Command {

    private UserService userService;

    public JoinCommand() {
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET 요청 처리 - 회원가입 폼 표시
        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.debug("회원가입 폼 표시");

            // 이미 로그인된 사용자인 경우 메인 페이지로 리다이렉트
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                log.debug("이미 로그인된 사용자의 회원가입 페이지 접근 시도");
                return "redirect:" + request.getContextPath() + "/";
            }

            // 회원가입 페이지로 포워드
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            return "/WEB-INF/views/user/join.jsp";
        }

        // POST 요청 처리 - 회원가입 로직 실행
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
            return "redirect:" + request.getContextPath() + "/login.user?registered=true";

        } catch (InvalidPasswordException e) {
            log.warn("회원가입 실패 - 비밀번호 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "/WEB-INF/views/user/join.jsp";
        } catch (InvalidEmailException e) {
            log.warn("회원가입 실패 - 이메일 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "/WEB-INF/views/user/join.jsp";
        } catch (DuplicateEmailException e) {
            log.warn("회원가입 실패 - 중복된 이메일: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "/WEB-INF/views/user/join.jsp";
        } catch (UserWithdrawnException e) {
            log.warn("회원가입 실패 - 탈퇴한 회원: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "/WEB-INF/views/user/join.jsp";
        } catch (RuntimeException e) {
            log.error("회원가입 처리 중 런타임 오류 발생: {}", e.getMessage(), e);

            // 오류 메시지를 좀 더 상세하게 처리
            String errorMessage = "회원가입 처리 중 시스템 오류가 발생했습니다.";
            if (e.getMessage() != null && e.getMessage().contains("데이터베이스")) {
                errorMessage = "데이터베이스 연결 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }

            request.setAttribute("errorMessage", errorMessage);
            return "/WEB-INF/views/user/join.jsp";
        } catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "/WEB-INF/views/user/join.jsp";
        }
    }
}