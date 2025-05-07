<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="domain.model.User" %>
<!doctype html>
<html>
<head>
    <title>회원 탈퇴</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/login.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css"/>
</head>
<body class="d-flex align-items-center py-4 bg-body-tertiary">
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>
<main class="form-signin w-100 m-auto">
    <form action="${pageContext.request.contextPath}/user/withdraw" method="post" accept-charset="UTF-8">
        <h1 class="h3 mb-3 fw-normal text-danger">회원 탈퇴</h1>

        <% if(request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger" role="alert">
            <%= request.getAttribute("errorMessage") %>
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
        
        <div class="alert alert-warning" role="alert">
            <strong>주의:</strong> 회원 탈퇴 시 모든 정보가 삭제되며, 이 작업은 되돌릴 수 없습니다.
        </div>

        <div class="form-floating mt-2">
            <input type="password" class="form-control" id="floatingPassword" name="password" placeholder="비밀번호" required>
            <label for="floatingPassword">비밀번호 확인</label>
        </div>
        
        <div class="form-check mt-3">
            <input class="form-check-input" type="checkbox" id="confirmWithdraw" name="confirmWithdraw" value="yes" required>
            <label class="form-check-label" for="confirmWithdraw">
                회원 탈퇴에 따른 정보 삭제에 동의합니다.
            </label>
        </div>
        
        <button class="btn btn-danger w-100 py-2 mt-3" type="submit">회원 탈퇴</button>
        <div class="mt-3 text-center">
            <a href="${pageContext.request.contextPath}/user/modify" class="text-primary">회원정보 수정으로 돌아가기</a>
            <span class="mx-2">|</span>
            <a href="/">메인으로 돌아가기</a>
        </div>
    </form>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
</body>
</html>