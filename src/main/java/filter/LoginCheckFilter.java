package filter;

import domain.dto.UserDTO;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginCheckFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        boolean isLoggedIn = false;
        boolean isAdmin = false;

        // 세션이 존재하고 사용자가 로그인되어 있는지 확인
        if (session != null && session.getAttribute("user") != null) {
            isLoggedIn = true;
            UserDTO userDTO = (UserDTO) session.getAttribute("user");


            if (session.getAttribute("isAdmin") != null) {
                isAdmin = (Boolean) session.getAttribute("isAdmin");
            }
        }

        if (isAdmin) {
            // 관리자인 경우, 요청한 페이지로 계속 진행
            chain.doFilter(request, response);
        } else if (isLoggedIn) {
            String alertScript = "<script>alert('관리자만 접근 가능합니다.'); history.back();</script>";
            httpResponse.setContentType("text/html;charset=UTF-8");
            httpResponse.getWriter().write(alertScript);
        } else {
            // 로그인이 안 된 경우, 현재 요청한 URL을 세션에 저장
            // 세션이 없으면 세션 생성
            if (session == null) {
                session = httpRequest.getSession(true);
            }

            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();

            // 쿼리 파라미터가 있는 경우 추가
            if (queryString != null && !queryString.isEmpty()) {
                requestURI += "?" + queryString;
            }

            // 원래 요청했던 URL을 세션에 저장
            session.setAttribute("redirectAfterLogin", requestURI);

            // 로그인 페이지로 리다이렉트
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/user/login");
        }
    }

    @Override
    public void destroy() {
        // 필터 종료 로직
    }
}