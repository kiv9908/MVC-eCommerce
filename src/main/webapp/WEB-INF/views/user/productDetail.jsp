<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>상품 상세 - ${product.productName}</title>
  <!-- Google Fonts - Noto Sans KR -->
  <link
          href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap"
          rel="stylesheet"
  />
  <!-- 부트스트랩 CSS -->
  <link
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet"
  />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/main.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/footer.css">
  <style>
    body {
      font-family: "Noto Sans KR", sans-serif;
      background-color: #f8f9fa;
      color: #212529;
      padding-top: 56px;
    }
    .product-detail {
      background-color: white;
      border-radius: 10px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      padding: 2rem;
      margin-top: 2rem;
      margin-bottom: 2rem;
    }
    .product-image {
      width: 100%;
      max-width: 500px;
      height: auto;
      border-radius: 8px;
      object-fit: contain;
    }
    .product-title {
      font-weight: 700;
      color: #212529;
      margin-bottom: 1rem;
    }
    .product-price {
      font-size: 1.5rem;
      font-weight: 700;
      color: #6a11cb;
      margin-bottom: 1.5rem;
    }
    .original-price {
      text-decoration: line-through;
      color: #6c757d;
      font-size: 1.1rem;
      margin-right: 0.5rem;
    }
    .product-description {
      color: #6c757d;
      margin-bottom: 2rem;
    }
    .btn-primary {
      background-color: #6a11cb;
      border-color: #6a11cb;
      font-weight: 500;
      padding: 0.75rem 2rem;
    }
    .btn-primary:hover {
      background-color: #5a0db6;
      border-color: #5a0db6;
    }
    .product-features {
      margin-top: 2rem;
      padding-top: 2rem;
      border-top: 1px solid #dee2e6;
    }
    .feature-item {
      margin-bottom: 1rem;
    }
    .feature-title {
      font-weight: 500;
      color: #212529;
    }
    .feature-content {
      color: #6c757d;
    }
    .product-status {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      font-size: 0.875rem;
      font-weight: 600;
      border-radius: 50px;
      margin-bottom: 1rem;
    }
    .status-available {
      background-color: #d1e7dd;
      color: #0f5132;
    }
    .status-soldout {
      background-color: #f8d7da;
      color: #842029;
    }
    .status-discontinued {
      background-color: #e2e3e5;
      color: #41464b;
    }
    /* 수량 선택기 스타일 개선 */
    .quantity-control {
      display: flex;
      align-items: center;
      margin-bottom: 1.5rem;
      width: 100%;
      max-width: 150px;
    }
    .quantity-label {
      margin-right: 1rem;
      font-weight: 500;
    }
    .quantity-input {
      display: flex;
      border: 1px solid #dee2e6;
      border-radius: 4px;
      overflow: hidden;
    }
    .quantity-input button {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f8f9fa;
      border: none;
      font-size: 16px;
      cursor: pointer;
    }
    .quantity-input button:hover {
      background-color: #e9ecef;
    }
    .quantity-input input {
      width: 50px;
      height: 36px;
      text-align: center;
      border: none;
      border-left: 1px solid #dee2e6;
      border-right: 1px solid #dee2e6;
      font-size: 14px;
    }
    .quantity-input input:focus {
      outline: none;
    }
    /* 버튼 간격 조정 */
    .action-buttons {
      display: flex;
      gap: 10px;
    }
    .action-buttons button {
      flex: 1;
    }
  </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<div class="container">
  <div class="product-detail">
    <div class="row">
      <div class="col-md-6">
        <c:choose>
          <c:when test="${not empty product.fileId}">
            <img src="${pageContext.request.contextPath}/file/${product.fileId}"
                 class="product-image" alt="${product.productName}" />
          </c:when>
          <c:otherwise>
            <img src="${pageContext.request.contextPath}/assets/images/no-image.png"
                 class="product-image" alt="이미지 없음" />
          </c:otherwise>
        </c:choose>
      </div>
      <div class="col-md-6">
        <!-- 상품 상태 표시 -->
        <span class="product-status
                        ${product.status eq '판매중' ? 'status-available' : ''}
                        ${product.status eq '품절' ? 'status-soldout' : ''}
                        ${product.status eq '판매중지' ? 'status-discontinued' : ''}">
          ${product.status}
        </span>

        <h1 class="product-title">${product.productName}</h1>

        <!-- 가격 정보 -->
        <p class="product-price">
          <c:if test="${product.customerPrice > product.salePrice}">
                            <span class="original-price">
                                <fmt:formatNumber value="${product.customerPrice}" pattern="#,###" />원
                            </span>
          </c:if>
          <span class="sale-price">
                            <fmt:formatNumber value="${product.salePrice}" pattern="#,###" />원
                        </span>
        </p>

        <!-- 상품 설명 -->
        <p class="product-description">${product.detailExplain}</p>

        <!-- 배송비 정보 -->
        <p class="mb-3">
          <strong>배송비:</strong>
          <c:choose>
            <c:when test="${product.deliveryFee > 0}">
              <fmt:formatNumber value="${product.deliveryFee}" pattern="#,###" />원
            </c:when>
            <c:otherwise>
              무료배송
            </c:otherwise>
          </c:choose>
        </p>

        <!-- 재고 정보 -->
        <p class="mb-3">
          <strong>재고:</strong>
          <c:choose>
            <c:when test="${product.stock > 0}">
              ${product.stock}개
            </c:when>
            <c:otherwise>
              품절
            </c:otherwise>
          </c:choose>
        </p>

        <c:if test="${product.status eq '판매중'}">
          <!-- 제품 주문 폼 -->
          <form id="productForm" method="post">
            <!-- hidden 필드 -->
            <input type="hidden" name="productCode" value="${product.productCode}">
            <input type="hidden" name="userId" value="${sessionScope.user.userId}">

            <!-- 수량 선택 (개선된 디자인) -->
            <div class="d-flex align-items-center mb-4">
              <span class="quantity-label">수량:</span>
              <div class="quantity-input">
                <button type="button" onclick="decreaseQuantity()">-</button>
                <input type="number" id="quantity" name="quantity" value="1" min="1" max="${product.stock}" readonly>
                <button type="button" onclick="increaseQuantity()">+</button>
              </div>
            </div>

            <!-- 구매 버튼 -->
            <div class="action-buttons">
              <button type="button" class="btn btn-primary" onclick="addToCart()">장바구니 담기</button>
              <button type="button" class="btn btn-outline-primary" onclick="buyNow()">바로 구매</button>
            </div>
          </form>
        </c:if>

        <c:if test="${product.status ne '판매중'}">
          <div class="alert alert-secondary mt-3" role="alert">
            <c:choose>
              <c:when test="${product.status eq '품절'}">
                해당 상품은 현재 품절되었습니다. 재입고 시 구매 가능합니다.
              </c:when>
              <c:when test="${product.status eq '판매중지'}">
                해당 상품은 현재 판매가 중지되었습니다.
              </c:when>
              <c:otherwise>
                해당 상품은 현재 구매할 수 없습니다.
              </c:otherwise>
            </c:choose>
          </div>
        </c:if>
      </div>
    </div>

    <!-- 상품 상세 설명 -->
    <div class="product-features mt-5">
      <h3>상품 상세 정보</h3>
      <hr>
      <div class="mt-4">
        ${product.detailExplain}
      </div>
    </div>
  </div>
</div>

<!-- 푸터 포함 -->
<%@ include file="/WEB-INF/includes/footer.jsp" %>

<!-- 부트스트랩 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- 수량 조절 및 장바구니/구매 스크립트 -->
<script>
  // 수량 감소
  function decreaseQuantity() {
    const quantityInput = document.getElementById('quantity');
    let quantity = parseInt(quantityInput.value);
    if (quantity > 1) {
      quantityInput.value = quantity - 1;
    }
  }

  // 수량 증가
  function increaseQuantity() {
    const quantityInput = document.getElementById('quantity');
    let quantity = parseInt(quantityInput.value);
    const maxStock = ${product.stock};

    if (quantity < maxStock) {
      quantityInput.value = quantity + 1;
    } else {
      alert('재고 수량을 초과할 수 없습니다.');
    }
  }

  // 장바구니 담기
  function addToCart() {
    const form = document.getElementById('productForm');
    // 폼 액션 변경 후 제출
    form.action = '${pageContext.request.contextPath}/user/basket.do/add';
    form.submit();
  }

  // 바로 구매
  function buyNow() {
    const form = document.getElementById('productForm');

    // 사용자 로그인 체크 (선택적)
    <c:if test="${empty sessionScope.user}">
    alert('로그인이 필요한 서비스입니다.');
    location.href = '${pageContext.request.contextPath}/user/login?redirect=' + encodeURIComponent(window.location.href);
    return;
    </c:if>

    // 폼 액션 변경 후 제출
    form.action = '${pageContext.request.contextPath}/user/order/order.do';
    form.submit();
  }
</script>
</body>
</html>