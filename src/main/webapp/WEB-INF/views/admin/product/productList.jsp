<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>상품 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">
    <style>
        .product-thumbnail {
            width: 50px;
            height: 50px;
            object-fit: cover;
            border-radius: 4px;
        }
        .no-image-placeholder {
            width: 50px;
            height: 50px;
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
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- 사이드바 인클루드 -->
            <jsp:useBean id="pageId" scope="request" class="java.lang.String"/>
            <%
                request.setAttribute("pageId", "product");
            %>
            <%@ include file="/WEB-INF/includes/sidebar.jsp" %>


            <!-- 메인 콘텐츠 -->
            <div class="col-md-9 col-lg-10 px-4 py-3">
                <div class="d-flex justify-content-between flex-wrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">상품 관리</h1>
                    <!-- 상품 추가 버튼 -->
                    <div class="actions">
                        <a href="${pageContext.request.contextPath}/admin/product/create" class="btn btn-primary">
                            <i class="fas fa-plus me-1"></i> 상품 추가
                        </a>
                    </div>
                </div>

                    <!-- 알림 메시지 -->
                    <c:if test="${param.success eq 'create'}">
                        <div class="alert alert-success">상품이 성공적으로 등록되었습니다.</div>
                    </c:if>
                    <c:if test="${param.success eq 'update'}">
                        <div class="alert alert-success">상품이 성공적으로 수정되었습니다.</div>
                    </c:if>
                    <c:if test="${param.success eq 'delete'}">
                        <div class="alert alert-success">상품이 성공적으로 삭제되었습니다.</div>
                    </c:if>
                    <c:if test="${param.error eq 'delete'}">
                        <div class="alert alert-danger">상품 삭제 중 오류가 발생했습니다.</div>
                    </c:if>

                <!-- 검색 및 정렬 -->
                <div class="row mb-3">
                    <!-- 검색 -->
                    <div class="col-md-6">
                        <form action="${pageContext.request.contextPath}/admin/product/list" method="get">
                            <div class="input-group">
                                <input type="text" class="form-control" name="keyword" value="${searchKeyword}" placeholder="상품명 검색..." />
                                <button class="btn btn-outline-secondary" type="submit">
                                    <i class="fas fa-search"></i>
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- 정렬 -->
                    <div class="col-md-6">
                        <div class="btn-group float-end">
                            <a href="${pageContext.request.contextPath}/admin/product/list?sortBy=priceAsc"
                               class="btn btn-outline-secondary ${empty param.sortBy || param.sortBy eq 'priceAsc' ? 'active' : ''}">
                                가격 낮은순
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/product/list?sortBy=priceDesc"
                               class="btn btn-outline-secondary ${param.sortBy eq 'priceDesc' ? 'active' : ''}">
                                가격 높은순
                            </a>
                        </div>
                    </div>
                </div>

                    <!-- 상품 목록 테이블 -->
                    <div class="table-responsive">
                        <table class="table table-striped table-hover">
                            <thead>
                                <tr>
                                    <th>이미지</th>
                                    <th>상품코드</th>
                                    <th>상품명</th>
                                    <th>정가</th>
                                    <th>판매가</th>
                                    <th>재고</th>
                                    <th>상태</th>
                                    <th>기간</th>
                                    <th>작업</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="product" items="${searchKeyword != null ? searchResults : products}">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty product.fileId}">
                                                    <img src="${pageContext.request.contextPath}/file/${product.fileId}" 
                                                         alt="${product.productName}" 
                                                         class="product-thumbnail">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="no-image-placeholder">
                                                        <i class="fas fa-image"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${product.productCode}</td>
                                        <td>${product.productName}</td>
                                        <td><fmt:formatNumber value="${product.customerPrice}" pattern="#,###" />원</td>
                                        <td><fmt:formatNumber value="${product.salePrice}" pattern="#,###" />원</td>
                                        <td>${product.stock}</td>
                                        <td>
                                            <span class="badge ${product.status eq '판매중' ? 'bg-success' : product.status eq '품절' ? 'bg-danger' : 'bg-warning'}">
                                                ${product.status}
                                            </span>
                                        </td>
                                        <td>
                                            ${product.startDate} ~ ${product.endDate}
                                        </td>
                                        <td>
                                            <a class="btn btn-sm btn-outline-primary edit-btn me-1"
                                               href="${pageContext.request.contextPath}/admin/product/edit/${product.productCode}">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <a class="btn btn-sm btn-outline-danger delete-btn"
                                               href="#" 
                                               data-product-code="${product.productCode}"
                                               data-product-name="${product.productName}"
                                               onclick="return confirm('정말로 이 상품을 삭제하시겠습니까?');">
                                                <i class="fas fa-trash-alt"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty products}">
                                    <tr>
                                        <td colspan="9" class="text-center">등록된 상품이 없습니다.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
            </div>
        </div>
    </div>
    
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function() {
            // 삭제 버튼 클릭 이벤트
            $('.delete-btn').on('click', function(e) {
                e.preventDefault();
                var productCode = $(this).data('product-code');
                var productName = $(this).data('product-name');
                
                if (confirm('정말로 "' + productName + '" 상품을 삭제하시겠습니까?')) {
                    window.location.href = '${pageContext.request.contextPath}/admin/product/delete/' + productCode;
                }
            });
        });
    </script>
</body>
</html>