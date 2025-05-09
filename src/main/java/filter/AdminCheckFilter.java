package filter;

import domain.dto.UserDTO;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AdminCheckFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화 로직
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        boolean isAdmin = false;

        // 세션이 존재하고 사용자가 로그인되어 있는지 확인
        if (session != null && session.getAttribute("user") != null) {
            UserDTO userDTO = (UserDTO) session.getAttribute("user");

            // 또는 세션에 isAdmin 속성이 이미 설정되어 있는지 확인
            if (session.getAttribute("isAdmin") != null) {
                isAdmin = (Boolean) session.getAttribute("isAdmin");
            }
        }

        if (isAdmin) {
            // 관리자인 경우, 요청한 페이지로 계속 진행
            chain.doFilter(request, response);
        } else {
            // 관리자가 아닌 경우, 메인 페이지나 권한 오류 페이지로 리다이렉트
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/user/login");
        }
    }
}
