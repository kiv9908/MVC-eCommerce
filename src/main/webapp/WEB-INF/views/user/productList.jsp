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
    .product-status {
      font-size: 0.7rem; /* 상태 표시 크기 조정 */
      padding: 2px 6px;
    }
    .category-active {
      background-color: #f8f9fa;
      color: #212529;
      font-weight: bold;
      border-left: 3px solid #0d6efd;
    }
    .list-group-item.category-active {
      box-shadow: 0 0 5px rgba(13, 110, 253, 0.2);
    }
    .list-group-item.category-active:hover {
      background-color: #e9ecef;
    }
    /* 판매중이 아닌 상품 스타일 */
    .unavailable-product {
      opacity: 0.6;
      filter: grayscale(80%);
      position: relative;
      overflow: hidden;
      transition: none;
      box-shadow: none;
    }

    .unavailable-product:hover {
      transform: none;
      box-shadow: none;
    }

    /* 품절/판매중지 배지 스타일 */
    .unavailable-badge {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      background-color: rgba(0, 0, 0, 0.6);
      color: white;
      text-align: center;
      padding: 8px;
      font-weight: bold;
      z-index: 2;
    }

    /* 판매중이 아닌 상품의 카드 내용 스타일 */
    .unavailable-product .card-title,
    .unavailable-product .card-text,
    .unavailable-product .text-muted {
      color: #6c757d !important;
    }
  </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<!-- 히어로 섹션 -->
<%@ include file="/WEB-INF/includes/hero.jsp" %>

<div class="container mt-4">
  <div class="row">
    <!-- 카테고리 사이드바 -->
    <div class="col-md-2 mb-4">
      <div class="card">
        <div class="card-header bg-dark text-white">
          <h5 class="card-title mb-0">카테고리</h5>
        </div>
        <div class="list-group list-group-flush">
          <a href="${pageContext.request.contextPath}/user/product/list.do"
             class="list-group-item list-group-item-action ${empty param.categoryId ? 'category-active' : ''}">
            전체 상품
          </a>
          <c:forEach var="category" items="${categories}">
            <c:if test="${category.level eq 2}">
              <a href="${pageContext.request.contextPath}/user/product/list.do?categoryId=${category.id}"
                 class="list-group-item list-group-item-action ${category.id eq param.categoryId ? 'category-active' : ''}">
                  ${category.fullName}
              </a>
            </c:if>
          </c:forEach>
        </div>
      </div>
    </div>

    <!-- 상품 목록 -->
    <div class="col-md-9">
      <!-- 검색, 필터링, 상품 개수를 한 줄에 표시 -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row align-items-center">
            <!-- 상품 개수 표시 (왼쪽) -->
            <div class="col-md-2">
              <span class="fw-bold">총 ${pageDTO.totalCount}개 상품</span>
            </div>

            <!-- 검색 및 필터링 (오른쪽) -->
            <div class="col-md-10">
              <form action="${pageContext.request.contextPath}/user/product/list.do" method="get" class="row g-2 justify-content-end">
                <!-- 정렬 옵션 -->
                <div class="col-md-3">
                  <select name="sortBy" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="priceAsc" ${empty pageDTO.sortBy || pageDTO.sortBy eq 'priceAsc' ? 'selected' : ''}>가격 낮은순</option>
                    <option value="priceDesc" ${pageDTO.sortBy eq 'priceDesc' ? 'selected' : ''}>가격 높은순</option>
                  </select>
                </div>

                <!-- 검색창 -->
                <div class="col-md-6">
                  <div class="input-group">
                    <input type="text" class="form-control form-control-sm" name="keyword"
                           placeholder="상품명 검색" value="${pageDTO.keyword}">
                    <button class="btn btn-sm btn-dark" type="submit">검색</button>
                  </div>
                </div>

                <!-- 카테고리 ID가 있으면 hidden 필드로 포함 -->
                <c:if test="${not empty param.categoryId}">
                  <input type="hidden" name="categoryId" value="${param.categoryId}">
                </c:if>
              </form>
            </div>
          </div>
        </div>
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
            <!-- 표시할 상품 개수를 카운트하기 위한 변수 초기화 -->
            <c:set var="displayedProducts" value="0" />

            <c:forEach var="product" items="${products}">
              <c:set var="displayedProducts" value="${displayedProducts + 1}" />
              <div class="col">
                <!-- 판매중인 상품은 clickable, 아닌 상품은 회색 처리 및 클릭 불가 -->
                <div class="card product-card h-100 ${product.status eq '판매중' ? '' : 'unavailable-product'}"
                  ${product.status eq '판매중' ? 'onclick="location.href=\'' += pageContext.request.contextPath += '/user/product/detail.do?productCode=' += product.productCode += '\'"' : ''}
                     style="${product.status eq '판매중' ? 'cursor: pointer;' : ''}">

                  <!-- 상품 상태 표시 (품절, 판매중지인 경우에만) -->
                  <c:if test="${product.status ne '판매중'}">
                    <div class="unavailable-badge">
                        ${product.status}
                    </div>
                  </c:if>

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
                    <!-- 가격 정보 -->
                    <div class="d-flex justify-content-between align-items-center">
                      <div>
                        <c:if test="${product.customerPrice > product.salePrice}">
                          <del class="text-muted">
                            <fmt:formatNumber value="${product.customerPrice}" pattern="#,###" />원
                          </del>
                        </c:if>
                        <div class="fs-5 fw-bold ${product.status eq '판매중' ? 'text-primary' : 'text-muted'}">
                          <fmt:formatNumber value="${product.salePrice}" pattern="#,###" />원
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </c:forEach>

            <!-- 상품이 하나도 없는 경우 메시지 표시 -->
            <c:if test="${displayedProducts == 0}">
              <div class="col-12">
                <div class="alert alert-info" role="alert">
                  상품이 없습니다.
                </div>
              </div>
            </c:if>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- 페이지네이션 -->
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
    </div>
  </div>
</div>

<!-- 푸터 포함 -->
<%@ include file="/WEB-INF/includes/footer.jsp" %>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>