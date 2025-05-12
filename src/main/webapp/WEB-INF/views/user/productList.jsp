<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>상품 목록</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/main.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/footer.css">
  <style>
    .product-card {
      height: 100%;
      transition: transform 0.3s;
    }
    .product-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }
    .product-img {
      height: 160px; /* 이미지 높이 조정 */
      object-fit: cover;
    }

    .card-title {
      font-size: 1rem; /* 제목 크기 조정 */
      margin-bottom: 0.5rem;
    }
  </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<!-- 히어로 섹션 -->
<section class="hero-section text-center">
  <div class="container">
    <h1 class="display-4 mb-4 fw-bold">쇼핑을 더 쉽게, 더 즐겁게</h1>
    <p class="lead mb-5">다양한 상품을 합리적인 가격에 만나보세요. 지금 바로 쇼핑을 시작하세요!</p>
    <div class="d-grid gap-2 d-sm-flex justify-content-sm-center">
      <a href="${pageContext.request.contextPath}/product/list.do" class="btn btn-light btn-lg px-4 me-sm-3">상품 보기</a>
      <% if(session.getAttribute("isLoggedIn") == null || !(Boolean)session.getAttribute("isLoggedIn")) { %>
      <a href="${pageContext.request.contextPath}/user/join" class="btn btn-outline-light btn-lg px-4">회원가입</a>
      <% } %>
    </div>
  </div>
</section>

<div class="container mt-4">
  <div class="row">
    <!-- 카테고리 사이드바 -->
    <div class="col-md-3 mb-4">
      <div class="card">
        <div class="card-header bg-dark text-white">
          <h5 class="card-title mb-0">카테고리</h5>
        </div>
        <div class="list-group list-group-flush">
          <a href="${pageContext.request.contextPath}/user/product/list.do"
             class="list-group-item list-group-item-action ${empty categoryId ? 'category-active' : ''}">
            전체 상품
          </a>
          <c:forEach var="category" items="${categories}">
            <a href="${pageContext.request.contextPath}/user/product/list.do?categoryId=${category.id}"
               class="list-group-item list-group-item-action ${category.id eq categoryId ? 'category-active' : ''}">
                ${category.name}
            </a>
          </c:forEach>
        </div>
      </div>
    </div>

    <!-- 상품 목록 -->
    <div class="col-md-9">
      <!-- 검색 및 필터 영역 -->
      <div class="card mb-4">
        <div class="card-body">
          <form action="${pageContext.request.contextPath}/user/product/list.do" method="get" class="row g-3">
            <div class="col-md-8">
              <div class="input-group">
                <input type="text" class="form-control" name="keyword"
                       placeholder="상품명 검색" value="${pageDTO.keyword}">
                <button class="btn btn-dark" type="submit">검색</button>
              </div>
            </div>
            <div class="col-md-4">
              <select name="sortBy" class="form-select" onchange="this.form.submit()">
                <option value="" ${empty pageDTO.sortBy ? 'selected' : ''}>기본 정렬</option>
                <option value="priceAsc" ${pageDTO.sortBy eq 'priceAsc' ? 'selected' : ''}>가격 낮은순</option>
                <option value="priceDesc" ${pageDTO.sortBy eq 'priceDesc' ? 'selected' : ''}>가격 높은순</option>
              </select>
            </div>
            <c:if test="${not empty categoryId}">
              <input type="hidden" name="categoryId" value="${categoryId}">
            </c:if>
          </form>
        </div>
      </div>

      <!-- 상품 개수 표시 -->
      <div class="mb-3">
        <span class="badge bg-secondary">총 ${pageDTO.totalCount}개 상품</span>
        <c:if test="${not empty pageDTO.keyword}">
          <span class="badge bg-info">"${pageDTO.keyword}" 검색결과</span>
        </c:if>
      </div>

      <!-- 상품 목록 표시 부분 -->
      <div class="row row-cols-1 row-cols-sm-2 row-cols-md-4 g-4">
        <c:choose>
          <c:when test="${empty products}">
            <div class="col-12">
              <div class="alert alert-info" role="alert">
                상품이 없습니다.
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <c:forEach var="product" items="${products}">
              <div class="col">
                <div class="card product-card h-100">
                  <!-- 상품 상태 표시 -->
                  <span class="product-status
                            ${product.status eq '판매중' ? 'status-available' : ''}
                            ${product.status eq '품절' ? 'status-soldout' : ''}
                            ${product.status eq '판매중지' ? 'status-discontinued' : ''}">
                      ${product.status}
                  </span>

                  <!-- 상품 이미지 -->
                  <c:choose>
                    <c:when test="${not empty product.fileId}">
                      <img src="${pageContext.request.contextPath}/file/${product.fileId}"
                           class="card-img-top product-img" alt="${product.productName}" loading="lazy">
                    </c:when>
                    <c:otherwise>
                      <img src="${pageContext.request.contextPath}/assets/images/no-image.png"
                           class="card-img-top product-img" alt="이미지 없음" loading="lazy">
                    </c:otherwise>
                  </c:choose>

                  <div class="card-body">
                    <h5 class="card-title">${product.productName}</h5>
                    <p class="card-text text-truncate">${product.detailExplain}</p>

                    <!-- 가격 정보 -->
                    <div class="d-flex justify-content-between align-items-center">
                      <div>
                        <c:if test="${product.customerPrice > product.salePrice}">
                          <del class="text-muted">
                            <fmt:formatNumber value="${product.customerPrice}" pattern="#,###" />원
                          </del>
                        </c:if>
                        <div class="fs-5 fw-bold text-primary">
                          <fmt:formatNumber value="${product.salePrice}" pattern="#,###" />원
                        </div>
                      </div>

                      <!-- 상세 페이지 링크 -->
                      <a href="${pageContext.request.contextPath}/user/product/detail/${product.productCode}"
                         class="btn btn-sm btn-outline-dark">상세보기</a>
                    </div>
                  </div>
                </div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- 페이지네이션 -->
      <c:if test="${pageDTO.totalPages > 1}">
        <nav class="mt-4" aria-label="상품 목록 페이지네이션">
          <ul class="pagination justify-content-center">
            <!-- 이전 페이지 -->
            <c:if test="${pageDTO.currentPage > 1}">
              <li class="page-item">
                <a class="page-link" href="${pageContext.request.contextPath}/user/product/list.do?page=${pageDTO.currentPage - 1}
                                        ${not empty pageDTO.sortBy ? '&sortBy='.concat(pageDTO.sortBy) : ''}
                                        ${not empty pageDTO.keyword ? '&keyword='.concat(pageDTO.keyword) : ''}
                                        ${not empty categoryId ? '&categoryId='.concat(categoryId) : ''}">
                  이전
                </a>
              </li>
            </c:if>

            <!-- 페이지 번호 -->
            <c:forEach begin="${pageDTO.startPage}" end="${pageDTO.endPage}" var="page">
              <li class="page-item ${page eq pageDTO.currentPage ? 'active' : ''}">
                <a class="page-link" href="${pageContext.request.contextPath}/user/product/list.do?page=${page}
                                        ${not empty pageDTO.sortBy ? '&sortBy='.concat(pageDTO.sortBy) : ''}
                                        ${not empty pageDTO.keyword ? '&keyword='.concat(pageDTO.keyword) : ''}
                                        ${not empty categoryId ? '&categoryId='.concat(categoryId) : ''}">
                    ${page}
                </a>
              </li>
            </c:forEach>

            <!-- 다음 페이지 -->
            <c:if test="${pageDTO.currentPage < pageDTO.totalPages}">
              <li class="page-item">
                <a class="page-link" href="${pageContext.request.contextPath}/user/product/list.do?page=${pageDTO.currentPage + 1}
                                        ${not empty pageDTO.sortBy ? '&sortBy='.concat(pageDTO.sortBy) : ''}
                                        ${not empty pageDTO.keyword ? '&keyword='.concat(pageDTO.keyword) : ''}
                                        ${not empty categoryId ? '&categoryId='.concat(categoryId) : ''}">
                  다음
                </a>
              </li>
            </c:if>
          </ul>
        </nav>
      </c:if>
    </div>
  </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>