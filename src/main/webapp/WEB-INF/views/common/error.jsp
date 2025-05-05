<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>접근 제한</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font.css">
</head>
<body>
    <div class="container mt-5">
        <div class="row">
            <div class="col-md-6 offset-md-3">
                <div class="card">
                    <div class="card-header bg-danger text-white">
                        <h4>접근 제한</h4>
                    </div>
                    <div class="card-body">
                        <div class="text-center mb-4">
                            <i class="fas fa-exclamation-triangle fa-4x text-warning"></i>
                        </div>
                        <h5 class="card-title text-center">권한이 없습니다</h5>
                        <p class="card-text text-center">
                            ${errorMessage != null ? errorMessage : '이 페이지에 접근할 수 있는 권한이 없습니다.'}
                        </p>
                        <div class="text-center mt-4">
                            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">홈으로 돌아가기</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
</body>
</html>
