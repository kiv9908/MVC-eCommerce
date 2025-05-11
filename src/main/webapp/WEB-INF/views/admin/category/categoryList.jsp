<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.net.URLEncoder" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>카테고리 관리</title>
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
      request.setAttribute("pageId", "category");
    %>
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

    <!-- 메인 콘텐츠 -->
    <div class="col-md-9 col-lg-10 px-4 py-3">
      <div class="d-flex justify-content-between flex-wrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">카테고리 관리</h1>
        <a href="${pageContext.request.contextPath}/admin/category/create" class="btn btn-primary">
          <i class="fas fa-plus me-1"></i> 새 카테고리 추가
        </a>
      </div>

      <!-- 알림 메시지 -->
      <c:if test="${not empty param.success}">
        <div class="alert alert-success alert-dismissible fade show">
          <c:choose>
            <c:when test="${param.success eq 'create'}">카테고리가 성공적으로 생성되었습니다.</c:when>
            <c:when test="${param.success eq 'update'}">카테고리가 성공적으로 수정되었습니다.</c:when>
            <c:when test="${param.success eq 'delete'}">카테고리가 성공적으로 삭제되었습니다.</c:when>
            <c:otherwise>작업이 성공적으로 완료되었습니다.</c:otherwise>
          </c:choose>
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      </c:if>

      <c:if test="${not empty param.error}">
        <div class="alert alert-danger alert-dismissible fade show">
          <c:choose>
            <c:when test="${param.error eq 'delete'}">카테고리 삭제 중 오류가 발생했습니다.</c:when>
            <c:otherwise>작업 중 오류가 발생했습니다.</c:otherwise>
          </c:choose>
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      </c:if>

      <!-- 카테고리 목록 테이블 -->
      <div class="table-responsive">
        <table class="table table-striped table-hover">
          <thead>
          <tr>
            <th>ID</th>
            <th>이름</th>
            <th>분류</th>
            <th>상위 카테고리</th>
            <th>전체 카테고리</th>
            <th>순서</th>
            <th>사용여부</th>
            <th>작업</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${(searchKeyword != null && not empty searchResults) || (searchKeyword == null && not empty categories)}">
              <!-- 카테고리가 있는 경우 -->
              <c:forEach var="category" items="${searchKeyword != null ? searchResults : categories}">
                <tr>
                  <td>${category.id}</td>
                  <td>${category.name}</td>
                  <td>
                      ${category.level == 0 ? '대분류' : category.level == 1 ? '중분류' : category.level == 2 ? '소분류' : ''}
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${category.parentId != null}">
                        ${category.parentId}
                      </c:when>
                      <c:otherwise>
                        <span class="text-muted">-</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${not empty category.fullName}">
                        ${category.fullName}
                      </c:when>
                      <c:otherwise>
                        <span class="text-muted">-</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>${category.order}</td>
                  <td>${category.useYn == 'Y' ? '사용' : '미사용'}</td>
                  <td>
                    <!-- 편집 버튼 -->
                    <a href="${pageContext.request.contextPath}/admin/category/edit/${category.id}"
                       class="btn btn-sm btn-outline-primary me-1 btn-action" title="카테고리 편집">
                      <i class="fas fa-edit"></i>
                    </a>
                    <!-- 삭제 버튼-->
                    <a href="${pageContext.request.contextPath}/admin/category/delete/${category.id}?page=${pageDTO.currentPage}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}"
                       class="btn btn-sm btn-outline-danger" title="카테고리 삭제">
                      <i class="fas fa-trash-alt"></i>
                    </a>
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <!-- 카테고리가 없는 경우 -->
              <tr>
                <td colspan="7" class="text-center py-4">
                  <div class="text-muted">
                    <p>등록된 카테고리가 없습니다.</p>
                  </div>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <c:if test="${pageDTO.totalCount > 0}">
        <div class="d-flex justify-content-center">
          <ul class="pagination">
            <!-- 이전 페이지 버튼 -->
            <li class="page-item ${pageDTO.currentPage == 1 ? 'disabled' : ''}">
              <a class="page-link" href="${pageContext.request.contextPath}/admin/category/list?page=${pageDTO.currentPage - 1}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
              </a>
            </li>

            <!-- 페이지 번호 -->
            <c:forEach begin="${pageDTO.startPage}" end="${pageDTO.endPage}" var="pageNum">
              <li class="page-item ${pageNum == pageDTO.currentPage ? 'active' : ''}">
                <a class="page-link" href="${pageContext.request.contextPath}/admin/category/list?page=${pageNum}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}">${pageNum}</a>
              </li>
            </c:forEach>

            <!-- 다음 페이지 버튼 -->
            <li class="page-item ${pageDTO.currentPage == pageDTO.totalPages ? 'disabled' : ''}">
              <a class="page-link" href="${pageContext.request.contextPath}/admin/category/list?page=${pageDTO.currentPage + 1}${not empty pageDTO.keyword ? '&keyword='.concat(URLEncoder.encode(pageDTO.keyword, 'UTF-8')) : ''}" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
              </a>
            </li>
          </ul>
        </div>

        <!-- 페이지 정보 표시 -->
        <div class="text-center mt-2 text-muted">
          <small>총 ${pageDTO.totalCount}개 카테고리 중 ${pageDTO.startRow} ~ ${pageDTO.endRow}개 표시</small>
        </div>
      </c:if>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</body>
</html>