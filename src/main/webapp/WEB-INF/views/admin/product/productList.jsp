<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.net.URLEncoder" %>
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
                                <input type="hidden" name="sortBy" value="${param.sortBy != null ? param.sortBy : 'priceAsc'}">
                                <input type="text" class="form-control" name="keyword" value="${param.keyword}" placeholder="상품명 검색">
                                <button class="btn btn-outline-secondary" type="submit">
                                    <i class="fas fa-search"></i>
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- 정렬 -->
                    <div class="col-md-6">
                        <div class="btn-group float-end">
                            <a href="${pageContext.request.contextPath}/admin/product/list?sortBy=priceAsc${not empty param.keyword ? '&keyword='.concat(param.keyword) : ''}${not empty param.page ? '&page='.concat(param.page) : ''}"
                               class="btn btn-outline-secondary ${empty param.sortBy || param.sortBy == 'priceAsc' ? 'active' : ''}">
                                가격 낮은순
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/product/list?sortBy=priceDesc${not empty param.keyword ? '&keyword='.concat(param.keyword) : ''}${not empty param.page ? '&page='.concat(param.page) : ''}"
                               class="btn btn-outline-secondary ${param.sortBy == 'priceDesc' ? 'active' : ''}">
                                가격 높은순
                            </a>
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
                            <c:choose>
                                <c:when test="${(searchKeyword != null && not empty searchResults) || (searchKeyword == null && not empty products)}">
                                    <!-- 상품이 있는 경우 -->
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
                                                   href="${pageContext.request.contextPath}/admin/product/delete/${product.productCode}?page=${currentPage}&sortBy=${param.sortBy != null ? param.sortBy : 'priceAsc'}&keyword=${not empty param.keyword ? URLEncoder.encode(param.keyword, 'UTF-8') : ''}"
                                                   data-product-code="${product.productCode}"
                                                   data-product-name="${product.productName}"
                                                >
                                                    <i class="fas fa-trash-alt"></i>
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- 상품이 없는 경우 -->
                                    <tr>
                                        <td colspan="9" class="text-center">등록된 상품이 없습니다.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>

                        <!-- 페이지네이션 -->
                        <div class="d-flex justify-content-center">
                            <ul class="pagination">
                                <!-- 이전 페이지 버튼 -->
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/product/list?page=${currentPage - 1}${not empty param.keyword ? '&keyword='.concat(param.keyword) : ''}&sortBy=${not empty param.sortBy ? param.sortBy : 'priceAsc'}" aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>

                                <!-- 페이지 번호 -->
                                <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                                    <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/admin/product/list?page=${pageNum}${not empty param.keyword ? '&keyword='.concat(param.keyword) : ''}&sortBy=${not empty param.sortBy ? param.sortBy : 'priceAsc'}">${pageNum}</a>
                                    </li>
                                </c:forEach>

                                <!-- 다음 페이지 버튼 -->
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/product/list?page=${currentPage + 1}${not empty param.keyword ? '&keyword='.concat(param.keyword) : ''}&sortBy=${not empty param.sortBy ? param.sortBy : 'priceAsc'}" aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </li>
                            </ul>
                        </div>

                        <!-- 페이지 정보 표시 -->
                        <div class="text-center mt-2 text-muted">
                            <c:choose>
                                <c:when test="${totalCount > 0}">
                                    <c:set var="endRowNum" value="${currentPage * pageSize}" />
                                    <c:if test="${endRowNum > totalCount}">
                                        <c:set var="endRowNum" value="${totalCount}" />
                                    </c:if>
                                    <small>총 ${totalCount}개 상품 중 ${(currentPage-1) * pageSize + 1} ~ ${endRowNum}개 표시</small>
                                </c:when>
                                <c:otherwise>
                                    <small>표시할 상품이 없습니다.</small>
                                </c:otherwise>
                            </c:choose>
                        </div>


                    </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>