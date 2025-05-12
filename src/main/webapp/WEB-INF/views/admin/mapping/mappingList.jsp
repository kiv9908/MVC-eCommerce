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
          <thead class="table-light">
          <tr>
            <th>상품명</th>
            <th>전체 카테고리</th>
            <th>표시 순서</th>
            <th>작업</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="mapping" items="${mappings}">
            <tr data-product-code="${mapping.productCode}" data-category-id="${mapping.categoryId}">
              <td>${mapping.productName}</td>
              <td>${mapping.fullName}</td>
              <td>${mapping.displayOrder}</td>
              <td>
                <!-- 편집 버튼 -->
                <a href="${pageContext.request.contextPath}/admin/mapping/edit/${mapping.productCode}/${mapping.categoryId}"
                   class="btn btn-sm btn-outline-primary me-1" title="매핑 편집">
                  <i class="fas fa-edit"></i>
                </a>
                <!-- 삭제 버튼 -->
                <a href="${pageContext.request.contextPath}/admin/mapping/delete/${mapping.productCode}/${mapping.categoryId}"
                   class="btn btn-sm btn-outline-danger" title="매핑 삭제"
                   onclick="return confirm('정말로 이 매핑을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');">
                  <i class="fas fa-trash-alt"></i>
                </a>
              </td>
            </tr>
          </c:forEach>

          <c:if test="${empty mappings}">
            <tr>
              <td colspan="6" class="text-center py-4">
                <div class="text-muted">
                  <p>등록된 카테고리 매핑이 없습니다.</p>
                  <c:if test="${not empty param.keyword}">
                    <p>검색어: "${param.keyword}"에 대한 결과가 없습니다.</p>
                    <p><a href="${pageContext.request.contextPath}/admin/mapping/list" class="btn btn-sm btn-outline-secondary">전체 목록 보기</a></p>
                  </c:if>
                </div>
              </td>
            </tr>
          </c:if>




          </tbody>
        </table>

        <!-- 페이지네이션 -->
        <div class="d-flex justify-content-center mt-4">
          <ul class="pagination">
            <!-- 이전 페이지 버튼 -->
            <li class="page-item ${pageDTO.currentPage == 1 ? 'disabled' : ''}">
              <a class="page-link" href="${pageContext.request.contextPath}/admin/mapping/list?page=${pageDTO.currentPage - 1}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}${not empty pageDTO.sortBy ? '&sortBy='.concat(pageDTO.sortBy) : ''}" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
              </a>
            </li>

            <!-- 페이지 번호 -->
            <c:forEach begin="${pageDTO.startPage}" end="${pageDTO.endPage}" var="pageNum">
              <li class="page-item ${pageNum == pageDTO.currentPage ? 'active' : ''}">
                <a class="page-link" href="${pageContext.request.contextPath}/admin/mapping/list?page=${pageNum}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}${not empty pageDTO.sortBy ? '&sortBy='.concat(pageDTO.sortBy) : ''}">${pageNum}</a>
              </li>
            </c:forEach>

            <!-- 다음 페이지 버튼 -->
            <li class="page-item ${pageDTO.currentPage == pageDTO.totalPages ? 'disabled' : ''}">
              <a class="page-link" href="${pageContext.request.contextPath}/admin/mapping/list?page=${pageDTO.currentPage + 1}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}${not empty pageDTO.sortBy ? '&sortBy='.concat(pageDTO.sortBy) : ''}" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
              </a>
            </li>
          </ul>
        </div>

        <!-- 페이지 정보 표시 -->
        <div class="text-center mt-2 text-muted">
          <c:choose>
            <c:when test="${pageDTO.totalCount > 0}">
              <small>총 ${pageDTO.totalCount}개 매핑 중 ${pageDTO.startRow} ~ ${pageDTO.endRow}개 표시</small>
            </c:when>
            <c:otherwise>
              <small>표시할 매핑이 없습니다.</small>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>
</div>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>