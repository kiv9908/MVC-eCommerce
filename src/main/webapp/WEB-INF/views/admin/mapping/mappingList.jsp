<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>카테고리 매핑 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/common.css">

  </head>
  <body>
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
          <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h1 class="h2">카테고리 매핑 관리</h1>
            <div class="btn-toolbar mb-2 mb-md-0">
              <a href="${pageContext.request.contextPath}/admin/mapping/create" class="btn btn-primary">
                <i class="fas fa-plus me-1"></i> 새 매핑 추가
              </a>
            </div>
          </div>

          <!-- 알림 메시지 -->
          <c:if test="${not empty message}">
            <div class="alert alert-${messageType == 'error' ? 'danger' : 'success'} alert-dismissible fade show" role="alert">
              ${message}
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
          </c:if>

          <!-- 매핑 목록 테이블 -->
          <div class="table-responsive">
            <table class="table table-striped table-hover" id="mappingTable">
              <thead>
                <tr>
                  <th>상품코드</th>
                  <th>상품명</th>
                  <th>카테고리 ID</th>
                  <th>전체 카테고리</th>
                  <th>표시 순서</th>
                  <th>편집</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="mapping" items="${mappings}">
                  <tr data-product-code="${mapping.productCode}" data-category-id="${mapping.categoryId}">
                    <td>${mapping.productCode}</td>
                    <td>${mapping.productName}</td>
                    <td>${mapping.categoryId}</td>
                    <td>${mapping.fullName}</td>
                    <td>${mapping.displayOrder}</td>
                    <td>
                      <!-- 편집 버튼 -->
                      <a href="${pageContext.request.contextPath}/admin/mapping/edit/${mapping.productCode}/${mapping.categoryId}"
                         class="btn btn-sm btn-outline-primary me-1 btn-action" title="카테고리 편집">
                        <i class="fas fa-edit"></i>
                      </a>
                    </td>
                  </tr>
                </c:forEach>

                <c:if test="${empty mappings}">
                  <tr>
                    <td colspan="8" class="text-center py-4">
                      <div class="text-muted">
                        <p>등록된 카테고리 매핑이 없습니다.</p>
                      </div>
                    </td>
                  </tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  </body>
</html>