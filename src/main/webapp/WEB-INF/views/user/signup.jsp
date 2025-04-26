<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
<head>
    <title>회원가입 - 쇼핑몰</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta charset="UTF-8">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/signup.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nav.css"/>
</head>
<body class="d-flex align-items-center justify-content-center py-4">
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>
<main class="form-signin w-100 m-auto">
    <form action="${pageContext.request.contextPath}/user/signup" method="post" accept-charset="UTF-8">
        <h1 class="h3 mb-3">회원가입</h1>

        <% if(request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger" role="alert">
            <%= request.getAttribute("errorMessage") %>
        </div>
        <% } %>

        <div class="form-floating mb-2">
            <input type="text" class="form-control" id="floatingName" name="userName" placeholder="이름" required>
            <label for="floatingName">이름</label>
        </div>
        <div class="form-floating mb-2">
            <input type="email" class="form-control" id="floatingEmail" name="email" placeholder="이메일" required>
            <label for="floatingEmail">이메일</label>
        </div>
        <div class="form-floating mb-2">
            <input type="password" class="form-control" id="floatingPassword" name="password" placeholder="비밀번호" required>
            <label for="floatingPassword">비밀번호</label>
        </div>
        <div class="form-floating mb-2">
            <input type="text" class="form-control" id="floatingMobileNumber" name="mobileNumber" placeholder="휴대폰 번호" required>
            <label for="floatingMobileNumber">휴대폰 번호</label>
        </div>
        
        <div class="password-hint">
            비밀번호는 대문자, 소문자, 숫자를 모두 포함하여 5~15자여야 합니다.
        </div>
        
        <button class="btn btn-primary w-100 py-2" type="submit">회원가입</button>
        
        <div class="mt-4 text-center">
            <p>이미 계정이 있으신가요? <a href="${pageContext.request.contextPath}/user/login">로그인</a></p>
        </div>
        
        <div class="mt-3 text-center">
            <a href="${pageContext.request.contextPath}/" class="text-muted">← 메인페이지로 돌아가기</a>
        </div>
    </form>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
</body>
</html>