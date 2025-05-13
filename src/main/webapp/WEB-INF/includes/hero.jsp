<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="hero-section text-center">
    <div class="container">
        <h1 class="display-4 mb-4 fw-bold">쇼핑을 더 쉽게, 더 즐겁게</h1>
        <p class="lead mb-5">다양한 상품을 합리적인 가격에 만나보세요. 지금 바로 쇼핑을 시작하세요!</p>
        <div class="d-grid gap-2 d-sm-flex justify-content-sm-center">
            <a href="${pageContext.request.contextPath}/user/product/list.do" class="btn btn-light btn-lg px-4 me-sm-3">상품 보기</a>
            <% if(session.getAttribute("isLoggedIn") == null || !(Boolean)session.getAttribute("isLoggedIn")) { %>
            <a href="${pageContext.request.contextPath}/user/join" class="btn btn-outline-light btn-lg px-4">회원가입</a>
            <% } %>
        </div>
    </div>
</section>