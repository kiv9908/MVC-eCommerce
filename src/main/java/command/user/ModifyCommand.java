package command.user;

import command.Command;
import config.AppConfig;
import domain.dto.UserDTO;
import exception.AuthenticationException;
import exception.InvalidPasswordException;
import lombok.extern.slf4j.Slf4j;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class ModifyCommand implements Command {

    private UserService userService;

    public ModifyCommand(){
        AppConfig appConfig = AppConfig.getInstance();
        this.userService = appConfig.getUserService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET 요청 처리 - 회원정보 수정 폼 표시
        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.debug("회원정보 수정 폼 표시");

            // 로그인 여부 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                log.warn("로그인되지 않은 사용자가 회원정보 수정 페이지에 접근 시도");
                return "redirect:" + request.getContextPath() + "/user/login";
            }

            // 회원정보 수정 페이지로 포워드
            return "/WEB-INF/views/user/modify.jsp";
        }

        // POST 요청 처리 - 회원정보 수정 로직 실행
        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원정보 수정 시도");
            return "redirect:" + request.getContextPath() + "/user/login";
        }

        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        String userId = sessionUser.getUserId();

        // 요청 파라미터에서 수정할 정보 추출
        String userName = request.getParameter("userName");
        String mobileNumber = request.getParameter("mobileNumber");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");

        log.info("회원정보 수정 시도 - 사용자ID: {}", userId);

        try {
            // 사용자 정보 조회
            UserDTO userDTO = userService.getUserByEmail(userId);
            if (userDTO == null) {
                log.warn("회원정보 수정 실패 - 사용자를 찾을 수 없음: {}", userId);
                request.setAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return "/WEB-INF/views/user/modify.jsp";
            }

            // 기본 정보 업데이트
            userDTO.setUserName(userName);
            userDTO.setMobileNumber(mobileNumber);

            // 비밀번호 변경 처리
            if (currentPassword != null && !currentPassword.isEmpty() &&
                    newPassword != null && !newPassword.isEmpty()) {

                // 비밀번호 변경 시도
                userService.changePassword(userId, currentPassword, newPassword);
                log.info("비밀번호 변경 성공 - 사용자ID: {}", userId);
                
                // 비밀번호 변경 후 사용자 정보 다시 조회하여 세션 업데이트
                userDTO = userService.getUserByEmail(userId);
            } else if ((currentPassword != null && !currentPassword.isEmpty()) ||
                    (newPassword != null && !newPassword.isEmpty())) {
                // 현재 비밀번호나 새 비밀번호 중 하나만 입력한 경우
                log.warn("비밀번호 변경 실패 - 현재 비밀번호와 새 비밀번호를 모두 입력해야 함");
                request.setAttribute("errorMessage", "비밀번호를 변경하려면 현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
                return "/WEB-INF/views/user/modify.jsp";
            }

            // 기본 정보만 업데이트 (비밀번호 제외)
            if (currentPassword == null || currentPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty()) {
                userDTO.setUserName(userName);
                userDTO.setMobileNumber(mobileNumber);
                userService.modifyUser(userDTO);
            }

            // 세션 정보 업데이트
            session.setAttribute("user", userDTO);

            log.info("회원정보 수정 성공 - 사용자ID: {}", userId);

            // 성공 메시지 설정 및 회원정보 수정 페이지로 포워드
            request.setAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
            return "/WEB-INF/views/user/modify.jsp";

        } catch (AuthenticationException e) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: {}", e.getMessage());
            request.setAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "/WEB-INF/views/user/modify.jsp";
        } catch (InvalidPasswordException e) {
            log.warn("비밀번호 변경 실패 - 새 비밀번호 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            return "/WEB-INF/views/user/modify.jsp";
        } catch (Exception e) {
            log.error("회원정보 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}