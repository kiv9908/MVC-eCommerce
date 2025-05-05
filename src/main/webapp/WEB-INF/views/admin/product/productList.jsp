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
                                        <td>${product.productCode}</td>
                                        <td>${product.productName}</td>
                                        <td><fmt:formatNumber value="${product.customerPrice}" pattern="#,###" />원</td>
                                        <td><fmt:formatNumber value="${product.salePrice}" pattern="#,###" />원</td>
                                        <td>${product.stock}</td>
                                        <td>${product.status}</td>
                                        <td>
                                            ${product.startDate} ~ ${product.endDate}
                                        </td>
                                        <td>
                                            <a class="btn btn-sm btn-outline-primary edit-btn me-1"
                                               href="${pageContext.request.contextPath}/admin/product/edit/${product.productCode}">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty products}">
                                    <tr>
                                        <td colspan="8" class="text-center">등록된 상품이 없습니다.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
            </div>
        </div>
    </div>
    
    <!-- 삭제 확인 모달 -->
    <div id="deleteModal" class="modal">
        <div class="modal-content">
            <h3>상품 삭제 확인</h3>
            <p>정말로 "<span id="deleteProductName"></span>" 상품을 삭제하시겠습니까?</p>
            <div class="modal-actions">
                <button id="confirmDelete" class="btn btn-danger">삭제</button>
                <button id="cancelDelete" class="btn btn-secondary">취소</button>
            </div>
        </div>
    </div>
    
    <script>
        $(document).ready(function() {
            // 모달 관련 변수
            var deleteModal = document.getElementById('deleteModal');
            var deleteProductCode = '';
            
            // 삭제 버튼 클릭 시
            $('.delete-btn').on('click', function(e) {
                e.preventDefault();
                deleteProductCode = $(this).data('product-code');
                $('#deleteProductName').text($(this).data('product-name'));
                deleteModal.style.display = 'block';
            });
            
            // 삭제 확인
            $('#confirmDelete').on('click', function() {
                window.location.href = '${pageContext.request.contextPath}/admin/product/delete/' + deleteProductCode;
            });
            
            // 삭제 취소
            $('#cancelDelete').on('click', function() {
                deleteModal.style.display = 'none';
            });
            
            // 모달 외부 클릭 시 닫기
            $(window).on('click', function(e) {
                if (e.target == deleteModal) {
                    deleteModal.style.display = 'none';
                }
            });

        });
    </script>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

</body>
</html>