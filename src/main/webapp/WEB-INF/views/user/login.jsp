<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
    <head>
        <title>로그인 - 쇼핑몰</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta charset="UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
        <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/login.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css"/>
    </head>
    <body class="login-body d-flex align-items-center justify-content-center py-4">
        <!-- 네비게이션 바 포함 -->
        <%@ include file="/WEB-INF/includes/nav.jsp" %>
        <main class="form-signin w-100 m-auto">
            <form action="${pageContext.request.contextPath}/user/login" method="POST" accept-charset="UTF-8">
                <h1 class="h3 mb-3">로그인</h1>

                <% if(request.getParameter("registered") != null && request.getParameter("registered").equals("true")) { %>
                <div class="alert alert-success" role="alert">
                    회원가입이 완료되었습니다. 로그인해주세요.
                </div>
                <% } %>
                
                <% if(request.getParameter("withdrawn") != null && request.getParameter("withdrawn").equals("true")) { %>
                <div class="alert alert-info" role="alert">
                    회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.
                </div>
                <% } %>

                <% if(request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%= request.getAttribute("errorMessage") %>
                </div>
                <% } %>

                <div class="form-floating mb-3">
                    <input type="email" class="form-control" id="floatingInput" name="email" placeholder="이메일 주소" required>
                    <label for="floatingInput">이메일 주소</label>
                </div>
                <div class="form-floating">
                    <input type="password" class="form-control" id="floatingPassword" name="password" placeholder="비밀번호" required>
                    <label for="floatingPassword">비밀번호</label>
                </div>

                <button class="btn btn-primary w-100 py-2" type="submit">로그인</button>
                
                <div class="mt-4 text-center">
                    <p>계정이 없으신가요? <a href="${pageContext.request.contextPath}/user/join">회원가입</a></p>
                </div>
                
                <div class="mt-4 text-center">
                    <a href="${pageContext.request.contextPath}/" class="text-muted">← 메인페이지로 돌아가기</a>
                </div>
            </form>
        </main>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
    </body>
</html>