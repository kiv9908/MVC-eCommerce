<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>주문 상세 내역 - eCommerce MVC</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta charset="UTF-8">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/main.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/footer.css">
  <style>
    .page-header {
      background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
      color: white;
      padding: 3rem 0;
      margin-top: 56px; /* 네비게이션 바 높이만큼 마진 추가 */
      margin-bottom: 2rem;
    }

    .order-detail-container {
      background-color: white;
      border-radius: 10px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      padding: 2rem;
      margin-bottom: 2rem;
    }

    .order-status {
      font-weight: 700;
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.85rem;
      display: inline-block;
    }

    .status-completed {
      background-color: #d4edda;
      color: #155724;
    }

    .status-processing {
      background-color: #fff3cd;
      color: #856404;
    }

    .status-cancelled {
      background-color: #f8d7da;
      color: #721c24;
    }

    .status-pending {
      background-color: #e2e3e5;
      color: #383d41;
    }

    .section-heading {
      position: relative;
      padding-bottom: 0.5rem;
      margin-bottom: 1.5rem;
      margin-top: 1.5rem ;
      font-weight: 600;
    }

    .section-heading::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 50px;
      height: 3px;
      background-color: #6a11cb;
    }

    .info-section {
      background-color: #f8f9fa;
      border-radius: 8px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
    }

    .order-item {
      border-bottom: 1px solid #dee2e6;
      padding: 1.5rem 0;
    }

    .order-item:last-child {
      border-bottom: none;
    }

    .order-item-image {
      width: 100px;
      height: 100px;
      object-fit: cover;
      border-radius: 8px;
      background-color: #e9ecef;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #6c757d;
      font-size: 0.8rem;
      text-align: center;
    }

    .order-item-title {
      font-weight: 700;
      color: #212529;
      margin-bottom: 0.5rem;
    }

    .order-item-price {
      color: #6a11cb;
      font-weight: 500;
    }

    .label-text {
      color: #6c757d;
      font-size: 0.9rem;
      margin-bottom: 0.25rem;
    }

    .value-text {
      font-weight: 500;
      margin-bottom: 1rem;
    }

    .summary-row {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem 0;
      border-bottom: 1px solid #dee2e6;
    }

    .summary-row:last-child {
      border-bottom: none;
      font-weight: 700;
    }

    .btn-primary {
      background-color: #6a11cb;
      border-color: #6a11cb;
    }

    .btn-primary:hover {
      background-color: #5a0db6;
      border-color: #5a0db6;
    }

    .btn-outline-primary {
      color: #6a11cb;
      border-color: #6a11cb;
    }

    .btn-outline-primary:hover {
      background-color: #6a11cb;
      border-color: #6a11cb;
    }
  </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<!-- 페이지 헤더 -->
<header class="page-header">
  <div class="container">
    <div class="row align-items-center">
      <div class="col-md-12 text-center">
        <h1>주문 상세 내역</h1>
        <p class="lead">주문번호: ${order.orderId}</p>
      </div>
    </div>
  </div>
</header>

<div class="container">
  <div class="order-detail-container">
    <!-- 주문 기본 정보 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h5 class="section-heading mb-0">주문 상세 정보</h5>
      <c:choose>
        <c:when test="${order.orderStatus eq '50'}">
          <span class="order-status status-completed">배송완료</span>
        </c:when>
        <c:when test="${order.orderStatus eq '40'}">
          <span class="order-status status-processing">배송중</span>
        </c:when>
        <c:when test="${order.orderStatus eq '10'}">
          <span class="order-status status-pending">주문 완료</span>
        </c:when>
        <c:when test="${order.orderStatus eq '30'}">
          <span class="order-status status-pending">배송 전</span>
        </c:when>
        <c:when test="${order.orderStatus eq '60'}">
          <span class="order-status status-pending">주문 취소</span>
        </c:when>
        <c:otherwise>
          <span class="order-status status-pending">${order.orderStatus}</span>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="info-section">
      <div class="row">
        <div class="col-md-6">
          <div class="label-text">주문번호</div>
          <div class="value-text">${order.orderId}</div>
        </div>
        <div class="col-md-6">
          <div class="label-text">주문일자</div>
          <div class="value-text"><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm"/></div>
        </div>
      </div>
    </div>

    <!-- 주문 상품 정보 영역-->
    <h5 class="section-heading">주문 상품</h5>
    <c:forEach var="item" items="${orderItems}">
      <div class="order-item">
        <div class="row align-items-center">
          <div class="col-md-2">
            <c:choose>
              <c:when test="${not empty item.fileId}">
                <img src="${pageContext.request.contextPath}/file/${item.fileId}"
                     alt="${item.productName}"
                     class="img-fluid order-item-image">
              </c:when>
              <c:otherwise>
                <div class="order-item-image">
                  <i class="bi bi-image"></i><br>상품 이미지 없음
                </div>
              </c:otherwise>
            </c:choose>
          </div>
          <div class="col-md-7">
            <h5 class="order-item-title">${item.productName}</h5>
            <p class="text-muted mb-0">수량: ${item.quantity}개</p>
            <p class="text-muted mb-0">상품 금액: <fmt:formatNumber value="${item.unitPrice}" pattern="#,###"/>원</p>
          </div>
          <div class="col-md-3 text-end">
            <p class="order-item-price mb-0">
              <fmt:formatNumber value="${item.amount}" pattern="#,###"/>원
            </p>
          </div>
        </div>
      </div>
    </c:forEach>

    <!-- 주문자 정보 -->
    <h5 class="section-heading">주문자 정보</h5>
    <div class="info-section">
      <div class="row">
        <div class="col-md-6">
          <div class="label-text">주문자명</div>
          <div class="value-text">${order.orderPersonName}</div>
        </div>
      </div>
    </div>

    <!-- 배송 정보 -->
    <h5 class="section-heading">배송 정보</h5>
    <div class="info-section">
      <div class="row">
        <div class="col-md-6">
          <div class="label-text">수령인</div>
          <div class="value-text">${order.receiverName}</div>
        </div>
        <div class="col-md-6">
          <div class="label-text">연락처</div>
          <div class="value-text">${order.receiverTelno}</div>
        </div>
        <div class="col-12">
          <div class="label-text">배송 주소</div>
          <div class="value-text">
            (${order.deliveryZipno}) ${order.deliveryAddress}
          </div>
        </div>
        <div class="col-12">
          <div class="label-text">배송 요청사항</div>
          <div class="value-text">
            ${not empty order.deliverySpace ? order.deliverySpace : '요청사항 없음'}
          </div>
        </div>
        <c:if test="${order.orderStatus == 'ORD3' || order.orderStatus == 'ORD4'}">
          <div class="col-12">
            <div class="label-text">배송 기간</div>
            <div class="value-text">
                ${order.deliveryPeriod}일
            </div>
          </div>
        </c:if>
      </div>
    </div>

    <!-- 결제 정보 -->
    <h5 class="section-heading">결제 정보</h5>
    <div class="info-section">
      <div class="summary-row">
        <span>상품 금액</span>
        <span>
          <fmt:formatNumber value="${order.orderAmount - (order.deliveryFee != null ? order.deliveryFee : 0)}" pattern="#,###"/>원
        </span>
      </div>
      <div class="summary-row">
        <span>배송비</span>
        <span><fmt:formatNumber value="${order.deliveryFee != null ? order.deliveryFee : 0}" pattern="#,###"/>원</span>
      </div>
      <div class="summary-row">
        <span>결제 상태</span>
        <c:choose>
          <c:when test="${order.paymentStatus eq '20'}">결제 완료</c:when>
          <c:when test="${order.paymentStatus eq '70'}">결제 취소</c:when>
          <c:otherwise>${order.paymentStatus}</c:otherwise>
        </c:choose>
      </div>
      <div class="summary-row">
        <span>총 결제 금액</span>
        <span class="order-item-price"><fmt:formatNumber value="${order.orderAmount}" pattern="#,###"/>원</span>
      </div>
    </div>

    <!-- 버튼 -->
    <div class="d-flex justify-content-between mt-4">
      <a href="${pageContext.request.contextPath}/user/order/list.do" class="btn btn-outline-secondary">목록으로</a>
      <div>
        <c:if test="${order.orderStatus eq '30'}">
          <button type="button" class="btn btn-outline-danger" onclick="cancelOrder('${order.orderId}')">주문 취소</button>
        </c:if>
      </div>
    </div>
  </div>
</div>

<!-- 푸터 포함 -->
<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
<script>
  // 주문 취소
  function cancelOrder(orderId) {
    if(confirm('정말 주문을 취소하시겠습니까?')) {
      // 포스트 요청으로 처리하기 위한 폼 생성 및 제출
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '${pageContext.request.contextPath}/user/order/cancel.do';

      const hiddenField = document.createElement('input');
      hiddenField.type = 'hidden';
      hiddenField.name = 'orderId';
      hiddenField.value = orderId;

      form.appendChild(hiddenField);
      document.body.appendChild(form);
      form.submit();
    }
  }
</script>
</body>
</html>