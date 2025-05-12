<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>
        <c:choose>
            <c:when test="${empty product.productCode}">상품 등록</c:when>
            <c:otherwise>상품 수정</c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">
    <style>
        .product-image-preview {
            max-width: 300px;
            max-height: 300px;
            object-fit: contain;
            margin-top: 10px;
            border: 1px solid #ddd;
            padding: 5px;
            border-radius: 4px;
        }
    </style>
</head>
<body class="bg-light">

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
                    <h1 class="h2">
                        <c:choose>
                            <c:when test="${empty product.productCode}">상품 등록</c:when>
                            <c:otherwise>상품 수정</c:otherwise>
                        </c:choose>
                    </h1>
                </div>

            <!-- 오류 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">${errorMessage}</div>
            </c:if>

            <c:choose>
                <c:when test="${empty product.productCode}">
                    <!-- 상품 등록 폼 -->
                    <form action="${pageContext.request.contextPath}/admin/product/create" method="post" class="needs-validation" enctype="multipart/form-data" novalidate>
                </c:when>
                <c:otherwise>
                    <!-- 상품 수정 폼 -->
                    <form action="${pageContext.request.contextPath}/admin/product/edit" method="post" class="needs-validation" enctype="multipart/form-data" novalidate>
                    <input type="hidden" name="productCode" value="${product.productCode}">
                    <input type="hidden" name="fileId" value="${product.fileId}">
                    
                    <div class="mb-3">
                        <label>상품 코드</label>
                        <div class="form-control-plaintext">${product.productCode}</div>
                    </div>
                </c:otherwise>
            </c:choose>
                    
                    <div class="mb-3">
                        <label for="productName" class="form-label">상품명 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="productName" name="productName" value="${product.productName}" required>
                    </div>

                    <div class="mb-3">
                        <label for="detailExplain" class="form-label">상품 상세 설명</label>
                        <textarea class="form-control" id="detailExplain" name="detailExplain" rows="5">${product.detailExplain}</textarea>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="customerPrice" class="form-label">정가 (원)</label>
                            <input type="number" class="form-control" id="customerPrice" name="customerPrice" value="${product.customerPrice}" min="0">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="salePrice" class="form-label">판매가 (원) <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" id="salePrice" name="salePrice" value="${product.salePrice}" min="0" required>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="stock" class="form-label">재고 수량</label>
                            <input type="number" class="form-control" id="stock" name="stock" value="${product.stock}" min="0">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="deliveryFee" class="form-label">배송비 (원)</label>
                            <input type="number" class="form-control" id="deliveryFee" name="deliveryFee" value="${product.deliveryFee}" min="0">
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="startDate" class="form-label">판매 시작일 (YYYYMMDD)</label>
                            <input type="text" class="form-control" id="startDate" name="startDate" value="${product.startDate}" placeholder="예: 20250101">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="endDate" class="form-label">판매 종료일 (YYYYMMDD)</label>
                            <input type="text" class="form-control" id="endDate" name="endDate" value="${product.endDate}" placeholder="예: 20251231">
                        </div>
                    </div>

                    <!-- 카테고리 선택 섹션 -->
                    <div class="mb-4">
                        <label for="categorySelect" class="form-label">카테고리 <span class="text-danger">*</span></label>
                        <div class="card">
                            <div class="card-body">
                                <select class="form-select" id="categorySelect" name="categoryId" required>
                                    <option value="">카테고리 선택</option>
                                    <c:forEach items="${availableCategories}" var="category">
                                        <option value="${category.id}" 
                                            <c:if test="${not empty productCategoryMappings && productCategoryMappings[0].id eq category.id}">selected</c:if>
                                        >${category.fullName}</option>
                                    </c:forEach>
                                </select>
                                <div class="form-text">상품에 연결할 카테고리를 선택하세요. 한 개의 카테고리만 선택 가능합니다.</div>
                            </div>
                        </div>
                    </div>

                    <!-- 상품 이미지 업로드 영역 -->
                    <div class="mb-3">
                        <label for="productImage" class="form-label">상품 이미지</label>
                        <div class="input-group">
                            <input type="file" class="form-control" id="productImage" name="productImage" accept="image/*">
                            <c:if test="${not empty product.fileId}">
                                <span class="input-group-text" id="currentFileName">
                                    <span id="fileNameDisplay">파일명.jpg</span>
                                </span>
                            </c:if>
                        </div>
                        <div class="form-text">이미지 파일만 업로드 가능합니다. (최대 10MB)</div>
                        
                        <!-- 이미지 미리보기 영역 -->
                        <div id="imagePreviewContainer" class="mt-2">
                            <c:if test="${not empty product.fileId}">
                                <div class="original-image">
                                    <img src="${pageContext.request.contextPath}/file/${product.fileId}" alt="상품 이미지" class="product-image-preview">
                                    <div class="mt-2">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" id="fileDeleteOption" name="fileDeleteOption" value="delete">
                                            <label class="form-check-label" for="fileDeleteOption">
                                                이미지 삭제
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                            <!-- 새 이미지 미리보기가 여기에 추가될 것입니다 -->
                            <div id="newImagePreview" style="display: none;">
                                <img id="newImagePreviewImg" src="" alt="새 이미지 미리보기" class="product-image-preview">
                                <p class="mt-1 text-muted">업로드할 새 이미지</p>
                            </div>
                        </div>
                    </div>

                    <c:if test="${not empty product.productCode}">
                        <div class="mb-3">
                            <label for="statusAction" class="form-label">판매 상태 관리</label>
                            <select class="form-select" id="statusAction" name="statusAction">
                                <option value="none" selected>- 변경 없음 -</option>
                                <option value="start">판매 시작</option>
                                <option value="stop">판매 중지</option>
                                <option value="soldout">품절 처리</option>
                            </select>
                            <div class="form-text">현재 상태: <span class="badge ${product.status eq '판매중' ? 'bg-success' : product.status eq '품절' ? 'bg-danger' : 'bg-warning'}">${product.status}</span></div>
                        </div>
                    </c:if>

                    <div class="d-flex justify-content-end gap-2">
                        <c:choose>
                            <c:when test="${empty product.productCode}">
                                <button type="submit" class="btn btn-primary">상품 등록</button>
                            </c:when>
                            <c:otherwise>
                                <button type="submit" class="btn btn-primary">상품 수정</button>
                                <a href="${pageContext.request.contextPath}/admin/product/delete/${product.productCode}"
                                   class="btn btn-danger" title="상품 삭제"
                                   onclick="return confirm('정말로 이 상품을 삭제하시겠습니까?');">
                                    상품 삭제
                                </a>
                            </c:otherwise>
                        </c:choose>
                        <a href="javascript:history.back()" class="btn btn-secondary">취소</a>
                    </div>
                </form>

            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <!-- 외부 스크립트 파일 로드 전 context path 설정 -->
    <script>
        // 컨텍스트 경로를 전역 변수로 설정 (JS 파일에서 사용)
        var contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/admin/productEdit.js"></script>

</body>
</html>