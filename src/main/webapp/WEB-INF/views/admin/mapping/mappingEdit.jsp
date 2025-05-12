<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${empty mapping.id}">카테고리 매핑 생성</c:when>
            <c:otherwise>카테고리 매핑 수정</c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">
</head>
<body class="bg-light">
<div class="container-fluid">
    <div class="row">
        <!-- 사이드바 인클루드 -->
        <jsp:useBean id="pageId" scope="request" class="java.lang.String"/>
        <%
            request.setAttribute("pageId", "mapping");
        %>
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <!-- 메인 콘텐츠 -->
        <div class="col-md-9 col-lg-10 px-4 py-3">
            <div class="d-flex justify-content-between align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2">
                    <c:choose>
                        <c:when test="${empty mapping.id}">카테고리 매핑 생성</c:when>
                        <c:otherwise>카테고리 매핑 수정</c:otherwise>
                    </c:choose>
                </h1>
            </div>

            <!-- 오류 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        ${errorMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <!-- 매핑 폼 -->
            <c:choose>
            <c:when test="${empty mapping.id}">
            <!-- 매핑 생성 폼 -->
            <form action="${pageContext.request.contextPath}/admin/mapping/create" method="post" id="mappingForm" class="needs-validation" novalidate>
                </c:when>
                <c:otherwise>
                <!-- 매핑 수정 폼 -->
                <form action="${pageContext.request.contextPath}/admin/mapping/edit" method="post" id="mappingForm" class="needs-validation" novalidate>
                    <input type="hidden" name="id" value="${mapping.id}">
                    <input type="hidden" name="originalProductCode" value="${mapping.productCode}">
                    <input type="hidden" name="originalCategoryId" value="${mapping.categoryId}">
                    </c:otherwise>
                    </c:choose>

                    <!-- 상품 선택 (생성 모드에서만 선택 가능) -->
                    <div class="mb-3">
                        <label for="productCode" class="form-label required-field">상품</label>
                        <c:choose>
                            <c:when test="${empty mapping.id}">
                                <!-- 생성 모드: 상품 드롭다운 -->
                                <select class="form-select" id="productCode" name="productCode" required>
                                    <option value="" selected disabled>상품을 선택하세요</option>
                                    <c:forEach var="product" items="${products}">
                                        <option value="${product.productCode}" ${mapping.productCode == product.productCode ? 'selected' : ''}>
                                                ${product.productCode} - ${product.productName}
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="form-text">매핑할 상품을 선택하세요.</div>
                            </c:when>
                            <c:otherwise>
                                <!-- 수정 모드: 상품 정보 표시 (수정 불가) -->
                                <input type="hidden" name="productCode" value="${mapping.productCode}">
                                <div class="form-control-plaintext">
                                    <strong>${mapping.productCode}</strong> - ${mapping.productName}
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- 카테고리 선택 -->
                    <div class="mb-3">
                        <label for="categoryId" class="form-label required-field">카테고리</label>
                        <select class="form-select" id="categoryId" name="categoryId" required>
                            <option value="" selected disabled>카테고리를 선택하세요</option>
                            <c:forEach var="category" items="${categories}">
                                <c:if test="${category.useYn eq 'Y'}">
                                    <option value="${category.id}" ${mapping.categoryId == category.id ? 'selected' : ''}>
                                            ${category.fullName}
                                    </option>
                                </c:if>
                            </c:forEach>
                        </select>
                        <div class="form-text">상품에 매핑할 카테고리를 선택하세요.</div>
                    </div>

                    <!-- 표시 순서 -->
                    <div class="mb-3">
                        <label for="displayOrder" class="form-label">표시 순서</label>
                        <input type="number" class="form-control" id="displayOrder" name="displayOrder"
                               value="${mapping.displayOrder != null ? mapping.displayOrder : '1'}" min="1">
                        <div class="form-text">카테고리 내에서 상품이 표시될 순서를 입력하세요. 낮은 숫자가 먼저 표시됩니다.</div>
                    </div>

                    <!-- 등록자 정보 -->
                    <input type="hidden" name="registerUser" value="${sessionScope.user.userName}">


                    <!-- 버튼 영역 -->
                    <div class="d-flex justify-content-between mt-4">
                        <div class="ms-auto">
                            <a href="${pageContext.request.contextPath}/admin/mapping/list" class="btn btn-secondary me-2">취소</a>
                            <c:choose>
                                <c:when test="${empty mapping.id}">
                                    <button type="submit" class="btn btn-primary">매핑 생성</button>
                                </c:when>
                                <c:otherwise>
                                    <button type="submit" class="btn btn-primary">매핑 수정</button>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty mapping.id}">
                                <a href="${pageContext.request.contextPath}/admin/mapping/delete/${mapping.productCode}/${mapping.categoryId}"
                                   class="btn btn-danger" title="매핑 삭제"
                                   onclick="return confirm('정말로 이 매핑을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');">
                                    매핑 삭제
                                </a>
                            </c:if>
                        </div>
                    </div>
                </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>