<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>사용자 관리 - 관리자 페이지</title>
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
                <h1 class="h2">사용자 관리</h1>
                <div class="btn-toolbar mb-2 mb-md-0"></div>
            </div>

            <!-- 알림 메시지 -->
            <c:if test="${not empty message}">
                <div class="alert alert-${messageType == 'error' ? 'danger' : 'success'} alert-dismissible fade show" role="alert">
                        ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <!-- 검색 및 필터 -->
            <div class="row mb-3">
                <div class="col-md-6">
                    <div class="input-group">
                        <input type="text" class="form-control" id="searchInput" placeholder="이름 또는 이메일로 검색..." />
                        <button class="btn btn-outline-secondary" type="button" id="searchButton">
                            <i class="fas fa-search"></i>
                        </button>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="btn-group float-end">
                        <button type="button" class="btn btn-outline-secondary" id="filterAll">전체</button>
                        <button type="button" class="btn btn-outline-secondary" id="filterActive">정상</button>
                        <button type="button" class="btn btn-outline-secondary" id="filterRequest">요청</button>
                        <button type="button" class="btn btn-outline-secondary" id="filterInactive">해지</button>
                        <button type="button" class="btn btn-outline-secondary" id="filterStop">일시정지</button>
                    </div>
                </div>
            </div>

            <!-- 사용자 목록 테이블 -->
            <div class="table-responsive">
                <table class="table table-striped table-hover" id="userTable">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>연락처</th>
                        <th>권한</th>
                        <th>상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="user" items="${users}">
                        <tr data-status="${user.status}">
                            <td>${user.userId}</td>
                            <td>${user.userName}</td>
                            <td>${user.email}</td>
                            <td>${user.mobileNumber}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.userType == '20'}">
                                        <span>관리자</span>
                                    </c:when>
                                    <c:when test="${user.userType == '10'}">
                                        <span>일반사용자</span>
                                    </c:when>
                                    <c:when test="${user.userType == '00'}">
                                        <span>가입대기</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span>일반사용자</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.status == 'ST00'}">
                                        <span class="status-badge status-request">요청</span>
                                    </c:when>
                                    <c:when test="${user.status == 'ST01'}">
                                        <span class="status-badge status-active">정상</span>
                                    </c:when>
                                    <c:when test="${user.status == 'ST02'}">
                                        <span class="status-badge status-inactive">해지</span>
                                    </c:when>
                                    <c:when test="${user.status == 'ST03'}">
                                        <span class="status-badge status-inactive">일시정지</span>
                                    </c:when>
                                </c:choose>
                            </td>
                            <td>
                                <a class="btn btn-sm btn-outline-primary edit-btn me-1"
                                   href="${pageContext.request.contextPath}/admin/user/edit?userId=${user.userId}">
                                    <i class="fas fa-edit"></i>
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

<!-- Bootstrap JS-->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- 전역 변수 설정 -->
<script>
    // 컨텍스트 경로 및 사용자 정보를 전역 변수로 설정
    const contextPath = "${pageContext.request.contextPath}";
    const sessionUserId = "${sessionScope.user.userId}";
    const isAdmin = ${sessionScope.isAdmin};
</script>

<!-- 사용자 관리 스크립트 -->
<script src="${pageContext.request.contextPath}/js/admin/userList.js"></script>
</body>
</html>