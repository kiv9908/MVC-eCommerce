<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>카테고리 관리 - 관리자 페이지</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/manager.css">
</head>
<body>
<div class="container-fluid">
  <div class="row">
    <!-- 사이드바 인클루드 -->
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

    <!-- 메인 콘텐츠 -->
    <div class="col-md-9 col-lg-10 px-4 py-3">
      <div class="d-flex justify-content-between flex-wrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">카테고리 관리</h1>
        <a href="${pageContext.request.contextPath}/admin/category/create" class="btn btn-primary">+ 카테고리 추가</a>
      </div>

      <!-- 알림 메시지 -->
      <c:if test="${not empty message}">
        <div class="alert alert-${messageType == 'error' ? 'danger' : 'success'} alert-dismissible fade show">
            ${message}
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      </c:if>

      <!-- 검색 -->
      <div class="row mb-3">
        <form class="col-md-6" method="get" action="${pageContext.request.contextPath}/admin/category/list">
          <div class="input-group">
            <input type="text" class="form-control" name="keyword" value="${searchKeyword}" placeholder="카테고리명 검색..." />
            <button class="btn btn-outline-secondary" type="submit"><i class="fas fa-search"></i></button>
          </div>
        </form>
      </div>
      <%
        out.println("categories: " + request.getAttribute("categories"));
      %>


      <!-- 카테고리 목록 -->

      <div class="table-responsive">
        <table class="table table-striped table-hover">
          <thead>
          <tr>
            <th>ID</th>
            <th>이름</th>
            <th>설명</th>
            <th>상위 카테고리</th>
            <th>순서</th>
            <th>사용여부</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="category" items="${categories}">
            <tr>
              <td>${category.nbCategory}</td>
              <td>${category.nmCategory}</td>
              <td>${category.nmExplain}</td>
              <td>
                <c:choose>
                  <c:when test="${category.nbParentCategory != null}">
                    ${category.nbParentCategory}
                  </c:when>
                  <c:otherwise>-</c:otherwise>
                </c:choose>
              </td>
              <td>${category.cnOrder}</td>
              <td>
                  <span class="badge bg-${category.ynUse == 'Y' ? 'success' : 'secondary'}">
                      ${category.ynUse == 'Y' ? '사용' : '미사용'}
                  </span>
              </td>
              <td>
                <a href="${pageContext.request.contextPath}/admin/category/edit/${category.nbCategory}" class="btn btn-sm btn-outline-primary me-1">
                  <i class="fas fa-edit"></i>
                </a>
                <a href="${pageContext.request.contextPath}/admin/category/delete/${category.nbCategory}" class="btn btn-sm btn-outline-danger"
                   onclick="return confirm('정말 삭제하시겠습니까?');">
                  <i class="fas fa-trash"></i>
                </a>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
