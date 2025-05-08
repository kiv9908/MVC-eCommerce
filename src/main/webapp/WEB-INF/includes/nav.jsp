<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- 네비게이션 바 -->
<nav class="navbar navbar-expand-lg navbar-light bg-white fixed-top shadow-sm">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">쇼핑몰</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/product/list">상품 보기</a>
                </li>
                <% if(session.getAttribute("isLoggedIn") != null && (Boolean)session.getAttribute("isLoggedIn")) { %>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/user/modify">내 정보</a>
                    </li>
                    <% if(session.getAttribute("user") != null && ((domain.model.User)session.getAttribute("user")).getUserType().equals("20")) { %>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/user/list">관리자 페이지</a>
                    </li>
                    <% } %>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/user/logout">로그아웃</a>
                    </li>
                <% } else { %>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/user/login">로그인</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/user/join">회원가입</a>
                    </li>
                <% } %>
            </ul>
        </div>
    </div>
</nav>