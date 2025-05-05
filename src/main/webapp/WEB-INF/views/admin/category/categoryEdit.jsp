<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${empty category.id}">카테고리 등록</c:when>
            <c:otherwise>카테고리 수정</c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">
</head>
<body class="bg-light">
    <div class="container-fluid">
        <div class="row">
            <!-- 사이드바 인클루드 -->
            <jsp:useBean id="pageId" scope="request" class="java.lang.String"/>
            <%
                request.setAttribute("pageId", "category");
            %>
            <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

            <!-- 메인 콘텐츠 -->
            <div class="col-md-9 col-lg-10 px-4 py-3">
                <div class="d-flex justify-content-between align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">
                        <c:choose>
                            <c:when test="${empty category.id}">카테고리 등록</c:when>
                            <c:otherwise>카테고리 수정</c:otherwise>
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

                <c:choose>
                    <c:when test="${empty category.id}">
                        <!-- 카테고리 등록 폼 -->
                        <form action="${pageContext.request.contextPath}/admin/category/create" method="post" id="categoryForm" class="needs-validation" novalidate>
                    </c:when>
                    <c:otherwise>
                        <!-- 카테고리 수정 폼 -->
                        <form action="${pageContext.request.contextPath}/admin/category/edit" method="post" id="categoryForm" class="needs-validation" novalidate>
                        <input type="hidden" name="categoryId" value="${category.id}">

                        <div class="mb-3">
                            <label>카테고리 ID</label>
                            <div class="form-control-plaintext">${category.id}</div>
                        </div>
                    </c:otherwise>
                </c:choose>
                            
                            <!-- 카테고리명 -->
                            <div class="mb-3">
                                <label for="name" class="form-label required-field">카테고리명</label>
                                <input type="text" class="form-control" id="name" name="name"
                                       value="${category.name}" required>
                                <div class="form-text">카테고리의 이름을 입력하세요 (최대 50자)</div>
                            </div>

                            <!-- 상위 카테고리 -->
                            <div class="mb-3">
                                <label for="parentId" class="form-label">상위 카테고리</label>
                                <select class="form-select" id="parentId" name="parentId">
                                    <option value="">상위 카테고리 없음 (최상위 카테고리)</option>
                                    <c:forEach var="parent" items="${parentCategories}">
                                        <option value="${parent.id}"
                                            ${category.parentId == parent.id ? 'selected' : ''}>
                                                ${parent.name}
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="form-text">이 카테고리의 상위 카테고리를 선택하세요. 없으면 최상위 카테고리로 설정됩니다.</div>
                            </div>

                            <!-- 전체 카테고리명 -->
                            <div class="mb-3">
                                <label for="fullName" class="form-label">전체 카테고리명</label>
                                <input type="text" class="form-control" id="fullName" name="fullName"
                                       value="${category.fullName}">
                                <div class="form-text">카테고리의 전체 경로명을 입력하세요 (예: 상위카테고리 > 하위카테고리)</div>
                            </div>

                            <!-- 카테고리 설명 -->
                            <div class="mb-3">
                                <label for="description" class="form-label">설명</label>
                                <textarea class="form-control" id="description" name="description"
                                          rows="3">${category.description}</textarea>
                                <div class="form-text">카테고리에 대한 간략한 설명을 입력하세요 (최대 200자)</div>
                            </div>

                            <!-- 표시 순서 -->
                            <div class="mb-3">
                                <label for="order" class="form-label">표시 순서</label>
                                <input type="number" class="form-control" id="order" name="order"
                                       value="${category.order != null ? category.order : '1'}" min="1">
                                <div class="form-text">카테고리가 표시될 순서를 입력하세요. 낮은 숫자가 먼저 표시됩니다.</div>
                            </div>

                            <!-- 사용 여부 -->
                            <div class="mb-3">
                                <div class="form-check form-switch">
                                    <input class="form-check-input" type="checkbox" id="useYn" name="useYn" value="Y"
                                           ${empty category.useYn || category.useYn == 'Y' ? 'checked' : ''}>
                                    <label class="form-check-label" for="useYn">카테고리 사용 여부</label>
                                </div>
                            </div>

                            <!-- 버튼 영역 -->
                            <div class="d-flex justify-content-between mt-4">
                                <div class="ms-auto">
                                    <c:choose>
                                        <c:when test="${empty category.id}">
                                            <button type="submit" class="btn btn-primary">카테고리 등록</button>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="submit" class="btn btn-primary">카테고리 수정</button>
                                            <a href="${pageContext.request.contextPath}/admin/category/delete/${category.id}"
                                               class="btn btn-danger" title="매핑 삭제"> 카테고리 삭제
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                    <a href="${pageContext.request.contextPath}/admin/category/list" class="btn btn-secondary me-2">취소</a>
                                </div>
                            </div>
                    </form>
            </div>
        </div>
    </div>
    
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>