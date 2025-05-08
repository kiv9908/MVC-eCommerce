<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>사용자 정보 수정 - 관리자 페이지</title>
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
      request.setAttribute("pageId", "user");
    %>
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

    <!-- 메인 콘텐츠 -->
    <div class="col-md-9 col-lg-10 px-4 py-3">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">사용자 정보 수정</h1>
        <div class="btn-toolbar mb-2 mb-md-0"></div>
      </div>

      <!-- 알림 메시지 -->
      <c:if test="${not empty message}">
        <div class="alert alert-${messageType == 'error' ? 'danger' : 'success'} alert-dismissible fade show" role="alert">
            ${message}
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
      </c:if>

      <!-- 회원 정보 수정 폼 -->
      <div class="card">
        <div class="card-body">
          <form id="editUserForm" class="edit-form" action="${pageContext.request.contextPath}/admin/user/edit" method="post">
            <input type="hidden" id="userId" name="userId" value="${user.userId}">

            <div class="mb-3">
              <label for="userName" class="form-label">이름</label>
              <input type="text" class="form-control" id="userName" name="userName" value="${user.userName}">
            </div>

            <div class="mb-3">
              <label for="email" class="form-label">이메일</label>
              <input type="email" class="form-control" id="email" name="email" value="${user.email}" readonly>
            </div>

            <div class="mb-3">
              <label for="mobileNumber" class="form-label">연락처</label>
              <input type="tel" class="form-control" id="mobileNumber" name="mobileNumber" value="${user.mobileNumber}">
            </div>

            <div class="mb-3">
              <label for="userType" class="form-label">권한</label>
              <select class="form-select" id="userType" name="userType">
                <option value="10" ${user.userType == '10' ? 'selected' : ''}>일반사용자</option>
                <option value="20" ${user.userType == '20' ? 'selected' : ''}>관리자</option>
              </select>
            </div>

            <div class="mb-3">
              <label for="status" class="form-label">상태</label>
              <select class="form-select" id="status" name="status">
                <option value="ST00" ${user.status == 'ST00' ? 'selected' : ''}>요청</option>
                <option value="ST01" ${user.status == 'ST01' ? 'selected' : ''}>정상</option>
                <option value="ST02" ${user.status == 'ST02' ? 'selected' : ''}>해지</option>
                <option value="ST03" ${user.status == 'ST03' ? 'selected' : ''}>일시정지</option>
              </select>
            </div>

            <div class="d-flex justify-content-end gap-2">
              <button type="submit" class="btn btn-primary">저장</button>
              <a href="${pageContext.request.contextPath}/admin/user/list" class="btn btn-secondary">취소</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Bootstrap JS & jQuery -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>