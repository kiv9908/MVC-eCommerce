package controller;

import config.AppConfig;
import domain.model.User;
import exception.DuplicateEmailException;
import exception.InvalidEmailException;
import exception.InvalidPasswordException;
import exception.UserWithdrawnException;
import service.AuthService;
import service.UserService;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/user/*")
@Slf4j
public class userServlet extends HttpServlet {
    
    private AuthService authService;
    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // AppConfig를 통해 의존성 주입
        AppConfig appConfig = AppConfig.getInstance();
        this.authService = appConfig.getAuthService();
        this.userService = appConfig.getUserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 요청 및 응답의 문자 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getPathInfo();
        
        if (action == null) {
            action = "/login";  // 기본 액션 설정
        }
        
        log.debug("doGet 요청: {}", action);
        
        switch (action) {
            case "/login":
                showLoginForm(request, response);
                break;
            case "/signup":
                showSignupForm(request, response);
                break;
            case "/update":
                showUpdateForm(request, response);
                break;
            case "/withdraw":
                showWithdrawForm(request, response);
                break;
            case "/logout":
                logout(request, response);
                break;
            default:
                log.warn("지원하지 않는 GET 요청: {}", action);
                response.sendRedirect(request.getContextPath() + "/user/login");
                break;
        }
    }
    
    // 회원정보 수정 폼 표시
    private void showUpdateForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("회원정보 수정 폼 표시");
        
        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원정보 수정 페이지에 접근 시도");
            response.sendRedirect(request.getContextPath() + "/user/login");
            return;
        }
        
        // 회원정보 수정 페이지로 포워드
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
        dispatcher.forward(request, response);
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
    
    private void showSignupForm(HttpServletRequest request, HttpServletResponse response) 
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
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
        dispatcher.forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 요청 및 응답의 문자 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getPathInfo();
        
        if (action == null) {
            action = "/login";
        }

        log.debug("doPost 요청: {}", action);

        switch (action) {
            case "/login":
                processLogin(request, response);
                break;
            case "/signup":
                processSignup(request, response);
                break;
            case "/update":
                processUpdate(request, response);
                break;
            case "/withdrawProcess":
                processWithdraw(request, response);
                break;
            default:
                log.warn("지원하지 않는 POST 요청: {}", action);
                response.sendRedirect(request.getContextPath() + "/user/login");
                break;
        }
    }
    
    // 회원정보 수정 처리
    private void processUpdate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 로그인 여부 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인되지 않은 사용자가 회원정보 수정 시도");
            response.sendRedirect(request.getContextPath() + "/user/login");
            return;
        }
        
        User sessionUser = (User) session.getAttribute("user");
        String userId = sessionUser.getUserId();
        
        // 요청 파라미터에서 수정할 정보 추출
        String userName = request.getParameter("userName");
        String mobileNumber = request.getParameter("mobileNumber");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        
        log.info("회원정보 수정 시도 - 사용자ID: {}", userId);
        
        try {
            // 사용자 정보 조회
            User user = userService.getUserByEmail(userId);
            if (user == null) {
                log.warn("회원정보 수정 실패 - 사용자를 찾을 수 없음: {}", userId);
                request.setAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
                dispatcher.forward(request, response);
                return;
            }
            
            // 기본 정보 업데이트
            user.setUserName(userName);
            user.setMobileNumber(mobileNumber);
            
            // 비밀번호 변경 처리
            if (currentPassword != null && !currentPassword.isEmpty() && 
                newPassword != null && !newPassword.isEmpty()) {
                
                // 비밀번호 변경 시도
                userService.changePassword(userId, currentPassword, newPassword);
                log.info("비밀번호 변경 성공 - 사용자ID: {}", userId);
            } else if ((currentPassword != null && !currentPassword.isEmpty()) || 
                      (newPassword != null && !newPassword.isEmpty())) {
                // 현재 비밀번호나 새 비밀번호 중 하나만 입력한 경우
                log.warn("비밀번호 변경 실패 - 현재 비밀번호와 새 비밀번호를 모두 입력해야 함");
                request.setAttribute("errorMessage", "비밀번호를 변경하려면 현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
                dispatcher.forward(request, response);
                return;
            }
            
            // 사용자 정보 업데이트
            userService.updateUser(user);
            
            // 세션 정보 업데이트
            session.setAttribute("user", user);
            
            log.info("회원정보 수정 성공 - 사용자ID: {}", userId);
            
            // 성공 메시지 설정 및 회원정보 수정 페이지로 포워드
            request.setAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
            
        } catch (exception.AuthenticationException e) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: {}", e.getMessage());
            request.setAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
        } catch (InvalidPasswordException e) {
            log.warn("비밀번호 변경 실패 - 새 비밀번호 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("회원정보 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/update.jsp");
            dispatcher.forward(request, response);
        }
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
    
    private void processSignup(HttpServletRequest request, HttpServletResponse response) 
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
            user.setUserType("10"); // 일반 사용자로 설정
            
            // 회원가입 처리
            userService.register(user);
            
            log.info("회원가입 성공 - 이메일: {}", email);
            
            // 회원가입 성공 후 로그인 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/user/login?registered=true");
            
        } catch (InvalidPasswordException e) {
            log.warn("회원가입 실패 - 비밀번호 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
            dispatcher.forward(request, response);
        } catch (InvalidEmailException e) {
            log.warn("회원가입 실패 - 이메일 유효성 검사 실패: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
            dispatcher.forward(request, response);
        } catch (DuplicateEmailException e) {
            log.warn("회원가입 실패 - 중복된 이메일: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
            dispatcher.forward(request, response);
        } catch (UserWithdrawnException e) {
            log.warn("회원가입 실패 - 탈퇴한 회원: {}", e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
            dispatcher.forward(request, response);
        } catch (RuntimeException e) {
            log.error("회원가입 처리 중 런타임 오류 발생: {}", e.getMessage(), e);
            
            // 오류 메시지를 좀 더 상세하게 처리
            String errorMessage = "회원가입 처리 중 시스템 오류가 발생했습니다.";
            if (e.getMessage() != null && e.getMessage().contains("데이터베이스")) {
                errorMessage = "데이터베이스 연결 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            request.setAttribute("errorMessage", errorMessage);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/user/signup.jsp");
            dispatcher.forward(request, response);
        }
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
                if (isAdmin) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                } else {
                    response.sendRedirect(request.getContextPath() + "/");
                }
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

    private void logout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                log.info("로그아웃 - 사용자: {}", user.getUserId());
            }
            // 세션 무효화
            session.invalidate();
        }
        
        // 로그아웃 후 메인 페이지로 리다이렉트
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies
        
        response.sendRedirect(request.getContextPath() + "/");
    }
}