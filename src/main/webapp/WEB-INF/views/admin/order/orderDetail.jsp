<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>주문 상세 정보</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">
    <style>
        .badge-order-status {
            font-size: 0.9rem;
            padding: 0.4em 0.7em;
        }
        .order-info-card {
            margin-bottom: 20px;
            border-radius: 0.5rem;
            box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
        }
        .order-item {
            margin-bottom: 10px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .order-item:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }
        .order-item-image {
            width: 60px;
            height: 60px;
            object-fit: cover;
            border-radius: 4px;
        }
        .no-image-placeholder {
            width: 60px;
            height: 60px;
            background-color: #f0f0f0;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #888;
            font-size: 12px;
        }
    </style>
</head>
<body class="bg-light">

<div class="container-fluid">
    <div class="row">
        <!-- 사이드바 인클루드 -->
        <jsp:useBean id="pageId" scope="request" class="java.lang.String"/>
        <%
            request.setAttribute("pageId", "order");
        %>
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <!-- 메인 콘텐츠 -->
        <div class="col-md-9 col-lg-10 px-4 py-3">
            <div class="d-flex justify-content-between flex-wrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2">주문 상세 정보</h1>
                <div>
                    <a href="${pageContext.request.contextPath}/admin/order/list" class="btn btn-outline-secondary">
                        <i class="fas fa-list-ul me-1"></i> 주문 목록
                    </a>
                </div>
            </div>

            <!-- 오류 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">${errorMessage}</div>
            </c:if>

            <!-- 성공 메시지 -->
            <c:if test="${not empty message}">
                <div class="alert alert-success">${message}</div>
            </c:if>

            <c:if test="${not empty order}">
                <!-- 주문 기본 정보 -->
                <div class="card order-info-card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">주문 정보</h5>
                        <div>
                            <span class="badge bg-primary">주문번호: ${order.orderId}</span>
                            <c:choose>
                                <c:when test="${order.orderStatus eq 'ORD1'}">
                                    <span class="badge bg-info badge-order-status">결제완료</span>
                                </c:when>
                                <c:when test="${order.orderStatus eq 'ORD2'}">
                                    <span class="badge bg-warning badge-order-status">상품준비중</span>
                                </c:when>
                                <c:when test="${order.orderStatus eq 'ORD3'}">
                                    <span class="badge bg-primary badge-order-status">배송중</span>
                                </c:when>
                                <c:when test="${order.orderStatus eq 'ORD4'}">
                                    <span class="badge bg-success badge-order-status">배송완료</span>
                                </c:when>
                                <c:when test="${order.orderStatus eq 'ORD5'}">
                                    <span class="badge bg-danger badge-order-status">취소</span>
                                </c:when>
                                <c:when test="${order.orderStatus eq 'ORD6'}">
                                    <span class="badge bg-secondary badge-order-status">환불</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-dark badge-order-status">${order.orderStatus}</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <table class="table table-borderless">
                                    <tr>
                                        <th style="width: 35%;">주문일시</th>
                                        <td><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                                    </tr>
                                    <tr>
                                        <th>주문자</th>
                                        <td>${order.orderPersonName}</td>
                                    </tr>
                                    <tr>
                                        <th>주문자 ID</th>
                                        <td>${order.userId}</td>
                                    </tr>
                                    <tr>
                                        <th>결제 상태</th>
                                        <td>
                                            <c:choose>
                                                <c:when test="${order.paymentStatus eq '20'}">
                                                    <span>결제완료</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span>${order.paymentStatus}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <div class="col-md-6">
                                <table class="table table-borderless">
                                    <tr>
                                        <th style="width: 35%;">주문 금액</th>
                                        <td><fmt:formatNumber value="${order.orderAmount}" pattern="#,###" />원</td>
                                    </tr>
                                    <tr>
                                        <th>배송비</th>
                                        <td><fmt:formatNumber value="${order.deliveryFee}" pattern="#,###" />원</td>
                                    </tr>
                                    <tr>
                                        <th>총 결제 금액</th>
                                        <td><strong><fmt:formatNumber value="${order.orderAmount + order.deliveryFee}" pattern="#,###" />원</strong></td>
                                    </tr>
                                    <tr>
                                        <th>주문 상품 수</th>
                                        <td>${order.totalItemCount}개</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 배송 정보 -->
                <div class="card order-info-card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">배송 정보</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <table class="table table-borderless">
                                    <tr>
                                        <th style="width: 35%;">받는 사람</th>
                                        <td>${order.receiverName}</td>
                                    </tr>
                                    <tr>
                                        <th>연락처</th>
                                        <td>${order.receiverTelno}</td>
                                    </tr>
                                    <tr>
                                        <th>우편번호</th>
                                        <td>${order.deliveryZipno}</td>
                                    </tr>
                                    <tr>
                                        <th>배송 주소</th>
                                        <td>${order.deliveryAddress}</td>
                                    </tr>
                                    <tr>
                                        <th>배송 메모</th>
                                        <td>${order.deliverySpace}</td>
                                    </tr>
                                    <tr>
                                        <th>배송 기간</th>
                                        <td>${order.deliveryPeriod}일</td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 주문 상품 목록 -->
                <div class="card order-info-card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">주문 상품 목록</h5>
                    </div>
                    <div class="card-body">
                        <c:forEach var="item" items="${orderItems}" varStatus="status">
                            <div class="order-item d-flex">
                                <div class="me-3">
                                    <c:choose>
                                        <c:when test="${not empty item.fileId}">
                                            <img src="${pageContext.request.contextPath}/file/${item.fileId}"
                                                 alt="${item.productName}"
                                                 class="order-item-image">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="no-image-placeholder">
                                                <i class="fas fa-image"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="flex-grow-1">
                                    <div class="d-flex justify-content-between align-items-start">
                                        <div>
                                            <h6 class="mb-1">${item.productName}</h6>
                                            <p class="mb-1 text-muted">상품코드: ${item.productCode}</p>
                                        </div>
                                        <div class="text-end">
                                            <p class="mb-0"><fmt:formatNumber value="${item.unitPrice}" pattern="#,###" />원 × ${item.quantity}개</p>
                                            <p class="fw-bold mb-0"><fmt:formatNumber value="${item.amount}" pattern="#,###" />원</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <c:if test="${empty orderItems}">
                            <div class="text-center py-3">
                                <p class="text-muted mb-0">주문 상품이 없습니다.</p>
                            </div>
                        </c:if>
                    </div>
                </div>

                <!-- 주문 상태 변경 폼 -->
                <div class="card order-info-card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">주문 상태 관리</h5>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/admin/order/update" method="post" class="needs-validation" novalidate>
                            <input type="hidden" name="orderId" value="${order.orderId}">

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="newStatus" class="form-label">주문 상태</label>
                                    <select class="form-select" id="newStatus" name="newStatus" required>
                                        <option value="" disabled>주문 상태 선택</option>
                                        <c:forEach items="${orderStatusList}" var="status">
                                            <option value="${status[0]}" ${order.orderStatus eq status[0] ? 'selected' : ''}>${status[1]}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="paymentStatus" class="form-label">결제 상태</label>
                                    <select class="form-select" id="paymentStatus" name="paymentStatus" disabled>
                                        <c:forEach items="${paymentStatusList}" var="status">
                                            <option value="${status[0]}" ${order.paymentStatus eq status[0] ? 'selected' : ''}>${status[1]}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="d-flex justify-content-end gap-2 mt-3">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save me-1"></i> 상태 변경
                                </button>
                                <a href="${pageContext.request.contextPath}/admin/order/list" class="btn btn-secondary">
                                    <i class="fas fa-arrow-left me-1"></i> 목록으로
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
            </c:if>

            <c:if test="${empty order}">
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-circle me-2"></i> 존재하지 않는 주문이거나 조회 권한이 없습니다.
                </div>
                <div class="text-center mt-4">
                    <a href="${pageContext.request.contextPath}/admin/order/list" class="btn btn-primary">
                        <i class="fas fa-arrow-left me-1"></i> 주문 목록으로
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // 주문 상태 변경 시 확인 다이얼로그
    document.querySelector('form').addEventListener('submit', function(e) {
        var currentStatus = '${order.orderStatus}';
        var newStatus = document.getElementById('newStatus').value;

        if (currentStatus !== newStatus) {
            if (!confirm('주문 상태를 변경하시겠습니까?')) {
                e.preventDefault();
            }
        }
    });
</script>
</body>
</html>