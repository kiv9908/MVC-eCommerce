<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>쇼핑몰 - eCommerce MVC</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nav.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
</head>
<body>
    <!-- 네비게이션 바 포함 -->
    <%@ include file="/WEB-INF/includes/nav.jsp" %>

    <!-- 히어로 섹션 -->
    <section class="hero-section text-center">
        <div class="container">
            <h1 class="display-4 mb-4 fw-bold">쇼핑을 더 쉽게, 더 즐겁게</h1>
            <p class="lead mb-5">다양한 상품을 합리적인 가격에 만나보세요. 지금 바로 쇼핑을 시작하세요!</p>
            <div class="d-grid gap-2 d-sm-flex justify-content-sm-center">
                <a href="${pageContext.request.contextPath}/product/list" class="btn btn-light btn-lg px-4 me-sm-3">상품 보기</a>
                <% if(session.getAttribute("isLoggedIn") == null || !(Boolean)session.getAttribute("isLoggedIn")) { %>
                    <a href="${pageContext.request.contextPath}/user/signup" class="btn btn-outline-light btn-lg px-4">회원가입</a>
                <% } %>
            </div>
        </div>
    </section>

    <!-- 특징 섹션 -->
    <section class="py-5">
        <div class="container">
            <h2 class="text-center mb-5">쇼핑몰의 특별한 혜택</h2>
            <div class="row">
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon">🚚</div>
                        <h3>빠른 배송</h3>
                        <p>주문 후 최대 3일 이내에 상품을 받아보실 수 있습니다. 신속하고 안전한 배송 서비스를 제공합니다.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon">💰</div>
                        <h3>특별 할인</h3>
                        <p>회원가입 시 5,000원 할인 쿠폰을 드립니다. 다양한 프로모션과 이벤트로 더 저렴하게 쇼핑하세요.</p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card text-center">
                        <div class="feature-icon">🔒</div>
                        <h3>안전한 결제</h3>
                        <p>SSL 보안 인증으로 안전한 결제를 보장합니다. 다양한 결제 방법을 지원하여 편리하게 이용하세요.</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- 인기 상품 섹션 -->
    <section class="py-5 bg-light">
        <div class="container">
            <h2 class="text-center mb-5">인기 상품</h2>
            <div class="row">
                <div class="col-md-3 mb-4">
                    <div class="card h-100">
                        <div class="bg-secondary text-white d-flex align-items-center justify-content-center" style="height: 200px;">상품 이미지</div>
                        <div class="card-body">
                            <h5 class="card-title">베스트 상품 1</h5>
                            <p class="card-text">29,800원</p>
                            <a href="${pageContext.request.contextPath}/product/list" class="btn btn-primary btn-sm">자세히 보기</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-4">
                    <div class="card h-100">
                        <div class="bg-secondary text-white d-flex align-items-center justify-content-center" style="height: 200px;">상품 이미지</div>
                        <div class="card-body">
                            <h5 class="card-title">베스트 상품 2</h5>
                            <p class="card-text">39,900원</p>
                            <a href="${pageContext.request.contextPath}/product/list" class="btn btn-primary btn-sm">자세히 보기</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-4">
                    <div class="card h-100">
                        <div class="bg-secondary text-white d-flex align-items-center justify-content-center" style="height: 200px;">상품 이미지</div>
                        <div class="card-body">
                            <h5 class="card-title">베스트 상품 3</h5>
                            <p class="card-text">49,000원</p>
                            <a href="${pageContext.request.contextPath}/product/list" class="btn btn-primary btn-sm">자세히 보기</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-4">
                    <div class="card h-100">
                        <div class="bg-secondary text-white d-flex align-items-center justify-content-center" style="height: 200px;">상품 이미지</div>
                        <div class="card-body">
                            <h5 class="card-title">베스트 상품 4</h5>
                            <p class="card-text">24,500원</p>
                            <a href="${pageContext.request.contextPath}/product/list" class="btn btn-primary btn-sm">자세히 보기</a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/product/list" class="btn btn-outline-primary">모든 상품 보기</a>
            </div>
        </div>
    </section>

    <!-- 회원가입 유도 섹션 -->
    <% if(session.getAttribute("isLoggedIn") == null || !(Boolean)session.getAttribute("isLoggedIn")) { %>
    <section class="py-5">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-6">
                    <h2>지금 회원가입하고 특별한 혜택을 받으세요!</h2>
                    <p class="lead">회원가입 시 즉시 사용 가능한 5,000원 할인 쿠폰을 드립니다. 또한 회원 전용 이벤트와 프로모션 정보를 가장 먼저 받아보실 수 있습니다.</p>
                    <a href="${pageContext.request.contextPath}/user/signup" class="btn btn-primary">회원가입하기</a>
                </div>
                <div class="col-md-6">
                    <div class="bg-secondary rounded p-5 text-white d-flex align-items-center justify-content-center" style="height: 300px;">
                        <div class="text-center">
                            <h3>신규 회원 특별 혜택</h3>
                            <ul class="list-unstyled">
                                <li>✓ 5,000원 할인 쿠폰</li>
                                <li>✓ 무료 배송 쿠폰</li>
                                <li>✓ 회원 전용 이벤트</li>
                                <li>✓ 생일 축하 쿠폰</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <% } %>

    <!-- 푸터 포함 -->
    <%@ include file="/WEB-INF/includes/footer.jsp" %>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
</body>
</html>
