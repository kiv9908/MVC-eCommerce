<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="domain.model.User" %>
<!doctype html>
<html>
<head>
    <title>내 정보 수정</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/login.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css"/>
</head>
<body class="d-flex align-items-center bg-body-tertiary">
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>
<main class="form-signin w-100 m-auto">
    <form action="${pageContext.request.contextPath}/user/modify" method="post" accept-charset="UTF-8">
        <h1 class="h3 mb-3 fw-normal">내 정보 수정</h1>

        <% if(request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger" role="alert">
            <%= request.getAttribute("errorMessage") %>
        </div>
        <% } %>
        
        <% if(request.getAttribute("successMessage") != null) { %>
        <div class="alert alert-success" role="alert">
            <%= request.getAttribute("successMessage") %>
        </div>
        <% } %>
        
        <% 
            // 세션에서 사용자 정보 가져오기
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/user/login");
                return;
            }
        %>

        <div class="form-floating">
            <input type="text" class="form-control" id="floatingName" name="userName" value="<%= user.getUserName() %>" placeholder="이름" required>
            <label for="floatingName">이름</label>
        </div>
        <div class="form-floating mt-2">
            <input type="email" class="form-control" id="floatingEmail" value="<%= user.getEmail() %>" readonly>
            <label for="floatingEmail">이메일 (변경 불가)</label>
        </div>
        <div class="form-floating mt-2">
            <input type="text" class="form-control" id="floatingMobileNumber" name="mobileNumber" value="<%= user.getMobileNumber() != null ? user.getMobileNumber() : "" %>" placeholder="휴대폰 번호" required>
            <label for="floatingMobileNumber">휴대폰 번호</label>
        </div>
        
        <div class="mt-3">
            <h4 class="h5">비밀번호 변경</h4>
            <p class="small text-muted">변경하려면 입력하세요. 변경하지 않으려면 비워두세요.</p>
        </div>
        
        <div class="form-floating mt-2">
            <input type="password" class="form-control" id="floatingCurrentPassword" name="currentPassword" placeholder="현재 비밀번호">
            <label for="floatingCurrentPassword">현재 비밀번호</label>
        </div>
        <div class="form-floating mt-2">
            <input type="password" class="form-control" id="floatingNewPassword" name="newPassword" placeholder="새 비밀번호">
            <label for="floatingNewPassword">새 비밀번호</label>
        </div>
        <div class="mt-3 text-center small text-muted">
            <p>새 비밀번호는 대문자, 소문자, 숫자를 모두 포함하여 5~15자여야 합니다.</p>
        </div>
        
        <button class="btn btn-primary w-100 py-2 mt-3" type="submit">정보 수정</button>
        <div class="mt-3 text-center">
            <a href="${pageContext.request.contextPath}/user/withdraw" class="text-danger">회원 탈퇴</a>
            <span class="mx-2">|</span>
            <a href="/">메인으로 돌아가기</a>
        </div>
    </form>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
</body>
</html>