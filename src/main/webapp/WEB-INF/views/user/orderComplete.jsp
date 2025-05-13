<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>주문 완료</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/footer.css">
    <style>
        .order-complete {
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            padding: 3rem;
            margin: 7em auto auto;
            text-align: center;
            max-width: 800px;
        }

        .order-complete h1 {
            color: #6a11cb;
            margin-bottom: 2rem;
        }

        .order-info {
            margin-bottom: 2.5rem;
            padding: 1.5rem;
            background-color: #f8f9fa;
            border-radius: 8px;
        }

        .order-info p {
            margin-bottom: 0.5rem;
            font-size: 1.1rem;
        }

        .order-success-icon {
            font-size: 5rem;
            color: #6a11cb;
            margin-bottom: 1.5rem;
        }

        .buttons {
            display: flex;
            justify-content: center;
            gap: 1rem;
            margin-top: 2rem;
        }

        .btn-primary {
            background-color: #6a11cb;
            border-color: #6a11cb;
            padding: 0.75rem 1.5rem;
            font-weight: 500;
        }

        .btn-primary:hover {
            background-color: #5a0db6;
            border-color: #5a0db6;
        }

        .btn-outline-primary {
            color: #6a11cb;
            border-color: #6a11cb;
            padding: 0.75rem 1.5rem;
            font-weight: 500;
        }

        .btn-outline-primary:hover {
            background-color: #6a11cb;
            border-color: #6a11cb;
        }
    </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<!-- 주문 완료 섹션 -->
<div class="container">
    <div class="order-complete">
        <div class="order-success-icon">✓</div>
        <h1>주문이 완료되었습니다</h1>

        <div class="order-info">
            <p>주문번호: <strong>${orderId}</strong></p>
            <p>주문해주셔서 감사합니다. 주문 내역은 마이페이지에서 확인하실 수 있습니다.</p>
            <p>결제확인 후 빠르게 배송해드리겠습니다.</p>
        </div>

        <div class="buttons">
            <a href="${pageContext.request.contextPath}/user/order/detail.do?orderId=${orderId}" class="btn btn-primary">주문 상세보기</a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-primary">홈으로</a>
        </div>
    </div>
</div>

<!-- 푸터 포함 -->
<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/js/bootstrap.bundle.min.js" integrity="sha384-k6d4wzSIapyDyv1kpU366/PK5hCdSbCRGRCMv+eplOQJWyd1fbcAu9OCUj5zNLiq" crossorigin="anonymous"></script>
</body>
</html>