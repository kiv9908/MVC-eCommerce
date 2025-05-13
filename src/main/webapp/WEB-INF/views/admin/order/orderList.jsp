<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.net.URLEncoder" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>주문 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">
    <style>
        .badge-order-status {
            font-size: 0.8rem;
            padding: 0.35em 0.65em;
        }
        .order-id {
            font-weight: 500;
            color: #0d6efd;
            text-decoration: none;
        }
        .order-id:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
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
                <h1 class="h2">주문 관리</h1>
            </div>

            <!-- 알림 메시지 -->
            <c:if test="${param.success eq 'update'}">
                <div class="alert alert-success">주문 상태가 성공적으로 업데이트되었습니다.</div>
            </c:if>
            <c:if test="${param.error eq 'update'}">
                <div class="alert alert-danger">주문 상태 업데이트 중 오류가 발생했습니다.</div>
            </c:if>

            <!-- 주문 목록 테이블 -->
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                    <tr>
                        <th>주문번호</th>
                        <th>주문일시</th>
                        <th>주문자</th>
                        <th>결제금액</th>
                        <th>상품수</th>
                        <th>주문상태</th>
                        <th>결제상태</th>
                        <th>상세보기</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty orderList}">
                            <!-- 주문이 있는 경우 -->
                            <c:forEach var="order" items="${orderList}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/order/detail/${order.orderId}"
                                           class="order-id">${order.orderId}</a>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td>${order.orderPersonName}</td>
                                    <td><fmt:formatNumber value="${order.orderAmount}" pattern="#,###" />원</td>
                                    <td>${order.totalItemCount}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.orderStatus eq '10'}">
                                                <span>주문완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq '30'}">
                                                <span>배송 전</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq '40'}">
                                                <span>배송 중</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq '50'}">
                                                <span>배송 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq '60'}">  <!-- 코드 일관성을 위해 '70'에서 '60'으로 수정 -->
                                                <span>주문 취소</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span>${order.orderStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.paymentStatus eq '20'}">
                                                <span>결제완료</span>
                                            </c:when>
                                            <c:when test="${order.paymentStatus eq '70'}">
                                                <span>결제취소</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span>${order.paymentStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a class="btn btn-sm btn-outline-primary"
                                           href="${pageContext.request.contextPath}/admin/order/detail/${order.orderId}">
                                            <i class="fas fa-search"></i> 상세
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <!-- 주문이 없는 경우 -->
                            <tr>
                                <td colspan="8" class="text-center">등록된 주문이 없습니다.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>

                <!-- 페이지네이션 -->
                <c:if test="${not empty orderList}">
                    <div class="d-flex justify-content-center">
                        <ul class="pagination">
                            <!-- 이전 페이지 버튼 -->
                            <li class="page-item ${pageDTO.currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/admin/order/list?page=${pageDTO.currentPage - 1}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}" aria-label="Previous">
                                    <span aria-hidden="true">&laquo;</span>
                                </a>
                            </li>

                            <!-- 페이지 번호 -->
                            <c:forEach begin="${pageDTO.startPage}" end="${pageDTO.endPage}" var="pageNum">
                                <li class="page-item ${pageNum == pageDTO.currentPage ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/order/list?page=${pageNum}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}">${pageNum}</a>
                                </li>
                            </c:forEach>

                            <!-- 다음 페이지 버튼 -->
                            <li class="page-item ${pageDTO.currentPage == pageDTO.totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/admin/order/list?page=${pageDTO.currentPage + 1}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}" aria-label="Next">
                                    <span aria-hidden="true">&raquo;</span>
                                </a>
                            </li>
                        </ul>
                    </div>

                    <!-- 페이지 정보 표시 -->
                    <div class="text-center mt-2 text-muted">
                        <small>총 ${pageDTO.totalCount}개 주문 중 ${pageDTO.startRow} ~ ${pageDTO.endRow}개 표시</small>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>