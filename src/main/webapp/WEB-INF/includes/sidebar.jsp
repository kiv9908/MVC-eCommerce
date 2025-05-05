<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
  <!-- 사이드바 -->
  <div class="col-md-3 col-lg-2 px-0 sidebar">
    <div class="d-flex flex-column flex-shrink-0 p-3">
      <a
              href="${pageContext.request.contextPath}/"
              class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none"
      >
        <span class="fs-4">쇼핑몰</span>
      </a>
      <hr />
      <ul class="nav nav-pills flex-column mb-auto">
        <li class="nav-item">
          <a href="${pageContext.request.contextPath}/admin/modify"
             class="nav-link ${pageId == 'user' ? 'active' : 'text-white'}">
            <i class="fas fa-users me-2"></i>
            사용자 관리
          </a>
        </li>
        <li>
          <a href="${pageContext.request.contextPath}/admin/product/list"
             class="nav-link ${pageId == 'product' ? 'active' : 'text-white'}">
            <i class="fas fa-box me-2"></i>
            상품 관리
          </a>
        </li>
        <li>
          <a href="${pageContext.request.contextPath}/admin/category"
             class="nav-link ${pageId == 'category' ? 'active' : 'text-white'}">
            <i class="fas fa-box me-2"></i>
            카테고리 관리
          </a>
        </li>
        <li>
          <a href="${pageContext.request.contextPath}/admin/mapping/list"
             class="nav-link ${pageId == 'mapping' ? 'active' : 'text-white'}">
            <i class="fas fa-box me-2"></i>
            카테고리 매핑 관리
          </a>
        </li>
        <li>
          <a href="#" class="nav-link text-white">
            <i class="fas fa-shopping-cart me-2"></i>
            주문 관리
          </a>
        </li>
      </ul>
    </div>
  </div>