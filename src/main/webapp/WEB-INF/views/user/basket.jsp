<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>장바구니</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/main.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css">
  <style>
    body {
      font-family: "Noto Sans KR", sans-serif;
      background-color: #f8f9fa;
      color: #212529;
      padding-top: 56px;
    }
    .basket-container {
      background-color: white;
      border-radius: 10px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      padding: 2rem;
      margin-top: 2rem;
    }
    .basket-item {
      border-bottom: 1px solid #dee2e6;
      padding: 1.5rem 0;
    }
    .basket-item:last-child {
      border-bottom: none;
    }
    .basket-item-image {
      width: 100px;
      height: 100px;
      object-fit: cover;
      border-radius: 8px;
    }
    .basket-item-title {
      font-weight: 700;
      color: #212529;
      margin-bottom: 0.5rem;
    }
    .basket-item-price {
      color: #6a11cb;
      font-weight: 500;
    }
    .quantity-control {
      width: 120px;
    }
    .btn-primary {
      background-color: #6a11cb;
      border-color: #6a11cb;
      font-weight: 500;
    }
    .btn-primary:hover {
      background-color: #5a0db6;
      border-color: #5a0db6;
    }
    .basket-summary {
      background-color: #f8f9fa;
      border-radius: 8px;
      padding: 1.5rem;
      margin-top: 2rem;
    }
    .summary-title {
      font-weight: 700;
      color: #212529;
      margin-bottom: 1rem;
    }
    .summary-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 0.5rem;
      color: #6c757d;
    }
    .summary-total {
      font-weight: 700;
      color: #6a11cb;
      font-size: 1.2rem;
      margin-top: 1rem;
      padding-top: 1rem;
      border-top: 1px solid #dee2e6;
    }
    .empty-basket {
      text-align: center;
      padding: 3rem 0;
    }
    .empty-basket-icon {
      font-size: 3rem;
      margin-bottom: 1rem;
      color: #dee2e6;
    }
    .login-message {
      background-color: #e9ecef;
      border-radius: 8px;
      padding: 1.5rem;
      margin: 3rem 0;
      text-align: center;
    }
    /* 품절/판매중지 상품 스타일 */
    .unavailable-item {
      opacity: 0.6;
    }
    .status-label {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.8rem;
      margin-left: 10px;
      font-weight: normal;
    }
    .sold-out {
      background-color: #dc3545;
      color: white;
    }
    .discontinued {
      background-color: #6c757d;
      color: white;
    }
  </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<div class="container">
  <div class="basket-container">
    <h2 class="mb-4">장바구니</h2>

    <c:choose>
      <%-- 로그인되지 않은 경우 로그인/회원가입 유도 --%>
      <c:when test="${empty user}">
        <div class="login-message">
          <h4>로그인이 필요합니다</h4>
          <p>장바구니를 이용하시려면 로그인이 필요합니다.</p>
          <div class="mt-4">
            <a href="${pageContext.request.contextPath}/user/login" class="btn btn-primary me-2">로그인</a>
            <a href="${pageContext.request.contextPath}/user/join" class="btn btn-outline-primary">회원가입</a>
          </div>
        </div>
      </c:when>

      <%-- 로그인된 경우 --%>
      <c:otherwise>
        <%-- 장바구니가 비어있는 경우 --%>
        <c:if test="${empty basket.items}">
          <div class="empty-basket">
            <div class="empty-basket-icon">🛒</div>
            <h4>장바구니가 비어있습니다</h4>
            <p class="text-muted">원하는 상품을 장바구니에 담아보세요!</p>
            <a href="${pageContext.request.contextPath}/user/product/list.do" class="btn btn-primary mt-3">상품 보러가기</a>
          </div>
        </c:if>

        <%-- 장바구니에 상품이 있는 경우 --%>
        <c:if test="${not empty basket.items}">
          <form id="basketForm" action="${pageContext.request.contextPath}/user/basket.do/update" method="POST">
            <div class="basket-items">
              <c:forEach var="item" items="${basket.items}" varStatus="status">
                <%-- 상품 상태 확인 --%>
                <c:set var="productAvailable" value="true" />
                <c:set var="statusLabel" value="" />

                <c:forEach var="product" items="${productList}">
                  <c:if test="${product.productCode eq item.productCode}">
                    <c:choose>
                      <c:when test="${product.stock <= 0}">
                        <c:set var="productAvailable" value="false" />
                        <c:set var="statusLabel" value="품절" />
                        <c:set var="statusClass" value="sold-out" />
                      </c:when>
                      <c:when test="${product.status ne '판매중'}">
                        <c:set var="productAvailable" value="false" />
                        <c:set var="statusLabel" value="판매중지" />
                        <c:set var="statusClass" value="discontinued" />
                      </c:when>
                    </c:choose>
                  </c:if>
                </c:forEach>

                <div class="basket-item ${productAvailable ? '' : 'unavailable-item'}">
                  <div class="row align-items-center">
                    <div class="col-md-1">
                      <div class="form-check">
                        <input class="form-check-input item-checkbox" type="checkbox" name="itemId" value="${item.itemId}" id="item${status.index}"
                          ${productAvailable ? 'checked' : 'disabled'}>
                      </div>
                    </div>
                    <div class="col-md-2">
                      <c:choose>
                        <c:when test="${not empty item.fileId}">
                          <img src="${pageContext.request.contextPath}/file/${item.fileId}" class="basket-item-image" alt="${item.productName}" />
                        </c:when>
                        <c:otherwise>
                          <img src="https://noticon-static.tammolo.com/dgggcrkxq/image/upload/v1744763499/noticon/hmmkrdssveiagf90sxzc.png" class="basket-item-image" alt="${item.productName}" />
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <div class="col-md-5">
                      <h5 class="basket-item-title">
                          ${item.productName}
                        <c:if test="${not empty statusLabel}">
                          <span class="status-label ${statusClass}">${statusLabel}</span>
                        </c:if>
                      </h5>
                      <p class="basket-item-price"><fmt:formatNumber value="${item.price}" type="currency" currencySymbol="" />원</p>
                    </div>
                    <div class="col-md-3">
                      <div class="input-group quantity-control">
                        <button class="btn btn-outline-secondary decrease-btn" type="button" data-item-id="${item.itemId}" ${productAvailable ? '' : 'disabled'}>-</button>
                        <input type="text" class="form-control text-center quantity-input" name="quantity" data-item-id="${item.itemId}" value="${item.quantity}" ${productAvailable ? '' : 'disabled'} />
                        <button class="btn btn-outline-secondary increase-btn" type="button" data-item-id="${item.itemId}" ${productAvailable ? '' : 'disabled'}>+</button>
                      </div>
                    </div>
                    <div class="col-md-1">
                      <button type="button" class="btn btn-sm btn-outline-danger delete-item" data-item-id="${item.itemId}">삭제</button>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </div>

            <!-- 버튼 그룹 -->
            <div class="d-flex justify-content-between mt-4">
              <div>
                <button type="button" class="btn btn-outline-secondary" onclick="selectAllItems(true)">전체 선택</button>
                <button type="button" class="btn btn-outline-secondary ms-2" onclick="selectAllItems(false)">전체 해제</button>
              </div>
              <div>
                <button type="button" class="btn btn-outline-secondary me-2" onclick="clearBasket()">장바구니 비우기</button>
                <button type="button" class="btn btn-outline-danger me-2" onclick="deleteSelectedItems()">선택 상품 삭제</button>
                <button type="button" class="btn btn-primary" onclick="orderSelectedItems()">선택 상품 주문</button>
              </div>
            </div>
          </form>
        </c:if>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<!-- 부트스트랩 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

<script>
  // 전역 함수 정의
  // 선택 상품 삭제
  function deleteSelectedItems() {
    const checkedBoxes = document.querySelectorAll('input[name="itemId"]:checked');
    if (checkedBoxes.length === 0) {
      alert('삭제할 상품을 선택해주세요.');
      return;
    }

    if (confirm('선택한 상품을 장바구니에서 삭제하시겠습니까?')) {
      const form = document.getElementById('basketForm');
      form.action = '${pageContext.request.contextPath}/user/basket.do/delete';
      form.submit();
    }
  }

  // 장바구니 비우기
  function clearBasket() {
    if (confirm('장바구니를 비우시겠습니까?')) {
      location.href = '${pageContext.request.contextPath}/user/basket.do/clear';
    }
  }

  // 선택 상품 주문
  function orderSelectedItems() {
    const checkedBoxes = document.querySelectorAll('input[name="itemId"]:checked');
    if (checkedBoxes.length === 0) {
      alert('주문할 상품을 선택해주세요.');
      return;
    }

    const form = document.getElementById('basketForm');
    form.action = '${pageContext.request.contextPath}/user/order/form.do';
    form.submit();
  }

  // 전체 선택/해제 (품절/판매중지 상품 제외)
  function selectAllItems(select) {
    const checkboxes = document.querySelectorAll('.item-checkbox:not([disabled])');
    checkboxes.forEach(checkbox => {
      checkbox.checked = select;
    });
  }

  document.addEventListener('DOMContentLoaded', function() {
    // 개별 항목 삭제
    document.querySelectorAll('.delete-item').forEach(button => {
      button.addEventListener('click', function() {
        const itemId = this.getAttribute('data-item-id');
        if (confirm('이 상품을 장바구니에서 삭제하시겠습니까?')) {
          location.href = '${pageContext.request.contextPath}/user/basket.do/delete?itemId=' + itemId;
        }
      });
    });

    // 수량 감소 버튼 처리
    document.querySelectorAll('.decrease-btn').forEach(button => {
      button.addEventListener('click', function() {
        const itemId = this.getAttribute('data-item-id');
        const inputContainer = this.closest('.input-group');
        const input = inputContainer ? inputContainer.querySelector('.quantity-input') : null;

        if (input) {
          let value = parseInt(input.value);
          if (value > 1) { // 최소 수량은 1
            value -= 1;
            location.href = '${pageContext.request.contextPath}/user/basket.do/update?itemId=' + itemId + '&quantity=' + value;
          }
        }
      });
    });

    // 수량 증가 버튼 처리
    document.querySelectorAll('.increase-btn').forEach(button => {
      button.addEventListener('click', function() {
        const itemId = this.getAttribute('data-item-id');
        const inputContainer = this.closest('.input-group');
        const input = inputContainer ? inputContainer.querySelector('.quantity-input') : null;

        if (input) {
          let value = parseInt(input.value);
          value += 1;
          location.href = '${pageContext.request.contextPath}/user/basket.do/update?itemId=' + itemId + '&quantity=' + value;
        }
      });
    });
  });
</script>
</body>
</html>