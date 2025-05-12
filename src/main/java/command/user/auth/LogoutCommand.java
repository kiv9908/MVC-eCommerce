package command.user.auth;

import command.Command;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LogoutCommand implements Command {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 사용자 로그아웃 로깅
            Object user = session.getAttribute("user");
            if (user != null) {
                log.info("사용자 로그아웃: {}", user);
            }

            // 세션 무효화
            session.invalidate();
        }

        // 로그아웃 후 메인 페이지로 리다이렉트
        return "redirect:" + request.getContextPath() + "/";
    }
}