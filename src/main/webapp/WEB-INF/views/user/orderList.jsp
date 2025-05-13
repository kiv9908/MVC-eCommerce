<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>주문관리 - eCommerce MVC</title>
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

    .order-section {
      background-color: white;
      border-radius: 10px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      padding: 2rem;
      margin-bottom: 2rem;
    }

    .section-heading {
      position: relative;
      padding-bottom: 0.5rem;
      margin-bottom: 1.5rem;
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

    .order-item {
      background-color: white;
      border: 1px solid #dee2e6;
      border-radius: 10px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .order-item:hover {
      transform: translateY(-5px);
      box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
    }

    .order-date {
      color: #6c757d;
      font-size: 0.9rem;
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

    .order-product-info {
      margin-bottom: 1rem;
    }

    .order-product-details {
      margin-left: 1rem;
    }

    .order-actions {
      display: flex;
      gap: 0.5rem;
    }

    .pagination {
      margin-top: 2rem;
    }

    .pagination .page-item.active .page-link {
      background-color: #6a11cb;
      border-color: #6a11cb;
      color: white; /* 선택된 페이지 숫자 흰색으로 변경 */
    }

    .pagination .page-link {
      color: #6a11cb;
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

    /* 주문 상세 레이아웃 개선 */
    .order-info-container {
      display: flex;
      flex-direction: column;
      width: 100%;
    }

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 0.5rem;
      width: 100%;
    }

    .order-header-left, .order-header-right {
      display: flex;
      flex-direction: column;
    }

    .order-details {
      display: flex;
      flex-direction: column;
      margin-bottom: 0.5rem;
    }

    .order-address {
      text-align: right;
      margin-bottom: 1rem;
    }

    .btn-container {
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
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
        <h1>주문관리</h1>
        <p class="lead">지난 주문내역을 확인하고 관리하세요.</p>
      </div>
    </div>
  </div>
</header>

<div class="container">
  <div class="order-section">
    <h5 class="section-heading">주문내역</h5>

    <!-- 주문 목록 -->
    <c:if test="${empty orderList}">
      <div class="text-center py-5">
        <i class="bi bi-bag-x" style="font-size: 3rem; color: #dee2e6;"></i>
        <p class="mt-3 text-muted">주문내역이 없습니다.</p>
      </div>
    </c:if>

    <c:if test="${not empty orderList}">
      <c:forEach var="order" items="${orderList}">
        <div class="order-item">
          <div class="order-info-container">
            <!-- 주문 헤더 정보 (주문일자, 주문번호, 상태) -->
            <div class="order-header">
              <div class="order-header-left">
                <p class="order-date mb-0">
                  <fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm"/>
                </p>
                <h6 class="mb-0">주문번호: ${order.orderId}</h6>
              </div>
              <div class="order-header-right">
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
            </div>

            <!-- 주문 상세 정보 (상품, 금액) -->
            <div class="order-details">
              <div class="row">
                <div class="col-md-2">
                  <c:if test="${not empty representativeItems[order.orderId]}">
                    <c:set var="item" value="${representativeItems[order.orderId]}" />
                    <div class="order-product-image mb-2">
                      <c:choose>
                        <c:when test="${not empty item.fileId}">
                          <img src="${pageContext.request.contextPath}/file/${item.fileId}" alt="${item.productName}" class="img-fluid" style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px;">
                        </c:when>
                        <c:otherwise>
                          <div style="width: 80px; height: 80px; background-color: #e9ecef; border-radius: 4px; display: flex; align-items: center; justify-content: center;">
                            <i class="bi bi-image" style="font-size: 1.5rem; color: #adb5bd;"></i>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </c:if>
                </div>
                <div class="col-md-10">
                  <div class="fw-bold">
                    <c:if test="${not empty representativeItems[order.orderId]}">
                      ${representativeItems[order.orderId].productName}
                      <c:if test="${order.totalItemCount > 1}">
                        <span class="text-muted"> 외 ${order.totalItemCount - 1}개</span>
                      </c:if>
                    </c:if>
                    <c:if test="${empty representativeItems[order.orderId]}">
                      ${order.totalItemCount}개의 상품
                    </c:if>
                  </div>
                  <div class="mt-1">
                    <fmt:formatNumber value="${order.orderAmount}" pattern="#,###"/>원
                    <c:if test="${not empty order.paymentStatus}">
                  <span class="text-muted small ms-2">
                    <c:choose>
                      <c:when test="${order.paymentStatus eq '20'}">결제 완료</c:when>
                      <c:when test="${order.paymentStatus eq '70'}">결제 취소</c:when>
                      <c:otherwise>${order.paymentStatus}</c:otherwise>
                    </c:choose>
                  </span>
                    </c:if>
                  </div>
                </div>
              </div>
            </div>

            <!-- 배송지 정보 -->
            <div class="order-address">
              <p class="mb-1"><small><strong>배송지</strong></small></p>
              <p class="mb-0"><small>${order.deliveryAddress}</small></p>
            </div>

            <!-- 버튼 영역 -->
            <div class="btn-container">
              <a href="${pageContext.request.contextPath}/user/order/detail.do?orderId=${order.orderId}" class="btn btn-primary btn-sm">상세보기</a>

              <c:if test="${order.orderStatus eq '30'}">
                <button type="button" class="btn btn-outline-danger btn-sm"
                        onclick="cancelOrder('${order.orderId}')">주문취소</button>
              </c:if>
            </div>
          </div>
        </div>
      </c:forEach>
    </c:if>

      <!-- 페이지네이션 -->
      <c:if test="${not empty pageDTO && pageDTO.totalPages > 1}">
        <nav aria-label="Page navigation">
          <ul class="pagination justify-content-center">
            <c:if test="${pageDTO.currentPage > 1}">
              <li class="page-item">
                <a class="page-link" href="?page=${pageDTO.currentPage - 1}" aria-label="Previous">
                  <span aria-hidden="true">&laquo;</span>
                </a>
              </li>
            </c:if>

            <c:forEach begin="${pageDTO.startPage}" end="${pageDTO.endPage}" var="i">
              <li class="page-item ${pageDTO.currentPage == i ? 'active' : ''}">
                <a class="page-link" href="?page=${i}">${i}</a>
              </li>
            </c:forEach>

            <c:if test="${pageDTO.currentPage < pageDTO.totalPages}">
              <li class="page-item">
                <a class="page-link" href="?page=${pageDTO.currentPage + 1}" aria-label="Next">
                  <span aria-hidden="true">&raquo;</span>
                </a>
              </li>
            </c:if>
          </ul>
        </nav>
      </c:if>
  </div>
</div>
<!-- 푸터 포함 -->
<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
<script>
  // 주문 취소 확인
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