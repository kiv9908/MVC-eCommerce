<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>주문하기</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.5/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-SgOJa3DmI69IUzQ2PVdRZhwQ+dy64/BUtbMJw1MZ8t5HZApcHrRKUc4W0kG879m7" crossorigin="anonymous">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user/nav.css">
    <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
    <style>
        body {
            background-color: #f8f9fa;
            color: #212529;
            padding-top: 56px;
        }
        .order-container {
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            padding: 2rem;
            margin-top: 2rem;
            margin-bottom: 2rem;
        }
        .order-item {
            border-bottom: 1px solid #dee2e6;
            padding: 1.5rem 0;
        }
        .order-item:last-child {
            border-bottom: none;
        }
        .order-item-image {
            width: 100px;
            height: 100px;
            object-fit: cover;
            border-radius: 8px;
        }
        .order-item-title {
            font-weight: 700;
            color: #212529;
            margin-bottom: 0.5rem;
        }
        .order-item-price {
            color: #6a11cb;
            font-weight: 500;
        }
        .btn-primary {
            background-color: #6a11cb;
            border-color: #6a11cb;
            font-weight: 500;
        }
        .btn-primary:hover {
            background-color: #5a0db6;
            border-color: #5a0db6;
        }
        .order-summary {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 1.5rem;
            margin-top: 2rem;
        }
        .summary-title {
            font-weight: 700;
            color: #212529;
            margin-bottom: 1rem;
        }
        .summary-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 0.5rem;
            color: #6c757d;
        }
        .summary-total {
            display: flex;
            justify-content: space-between;
            font-weight: 700;
            color: #6a11cb;
            font-size: 1.2rem;
            margin-top: 1rem;
            padding-top: 1rem;
            border-top: 1px solid #dee2e6;
        }
    </style>
</head>
<body>
<!-- 네비게이션 바 포함 -->
<%@ include file="/WEB-INF/includes/nav.jsp" %>

<div class="container">
    <div class="order-container">
        <h2 class="mb-4">주문하기</h2>

        <form id="orderForm" action="${pageContext.request.contextPath}/user/order/order.do" method="POST" onsubmit="return validateForm()">
            <!-- 주문 상품 목록 -->
            <div class="order-items mb-5">
                <h4 class="mb-3">주문 상품</h4>
                <c:set var="totalPrice" value="0" />
                <c:set var="totalDeliveryFeeSum" value="0" />

                <c:forEach items="${orderItems}" var="item" varStatus="status">
                    <div class="order-item">
                        <div class="row align-items-center">
                            <div class="col-md-2">
                                <c:choose>
                                    <c:when test="${not empty item.fileId}">
                                        <img src="${pageContext.request.contextPath}/file/${item.fileId}"
                                             class="order-item-image" alt="${item.productName}" />
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/assets/images/no-image.png"
                                             class="order-item-image" alt="이미지 없음" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-md-5">
                                <h5 class="order-item-title">${item.productName}</h5>
                                <p class="order-item-price mb-0">
                                    <fmt:formatNumber value="${item.unitPrice}" pattern="#,###" />원
                                </p>
                                <small class="text-muted">수량: ${item.quantity}개</small>

                                <!-- 상품 정보 hidden 필드 -->
                                <input type="hidden" name="productCode" value="${item.productCode}" />
                                <input type="hidden" name="quantity" value="${item.quantity}" />
                                <input type="hidden" name="unitPrice" value="${item.unitPrice}" />
                                <input type="hidden" name="itemDeliveryFee" value="${item.deliveryFee}" />
                            </div>
                            <div class="col-md-3 text-center">
                                <p class="mb-0">배송비:</p>
                                <p class="mb-0">
                                    <c:choose>
                                        <c:when test="${item.deliveryFee > 0}">
                                            <fmt:formatNumber value="${item.deliveryFee}" pattern="#,###" />원
                                        </c:when>
                                        <c:otherwise>
                                            무료배송
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            <div class="col-md-2 text-end">
                                <p class="order-item-price mb-0">
                                    <fmt:formatNumber value="${item.amount + item.deliveryFee}" pattern="#,###" />원
                                </p>
                            </div>
                        </div>
                    </div>
                    <c:set var="totalPrice" value="${totalPrice + item.amount}" />
                    <c:set var="totalDeliveryFeeSum" value="${totalDeliveryFeeSum + item.deliveryFee}" />
                </c:forEach>
            </div>

            <!-- 주문자 정보 -->
            <div class="orderer-info mb-5">
                <h4 class="mb-3">주문자 정보</h4>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label for="orderPersonName" class="form-label">주문자명</label>
                        <input type="text" class="form-control" id="orderPersonName" name="orderPersonName"
                               value="${user.userName}" readonly>
                    </div>
                    <div class="col-md-6">
                        <label for="orderPersonTelno" class="form-label">주문자 연락처</label>
                        <input type="text" class="form-control" id="orderPersonTelno" name="orderPersonTelno"
                               value="${user.mobileNumber}" readonly>
                    </div>
                </div>
            </div>

            <!-- 배송 정보 -->
            <div class="delivery-info mb-5">
                <h4 class="mb-3">배송 정보</h4>
                <div class="form-check mb-3">
                    <input type="checkbox" class="form-check-input" id="sameAsOrderer" onclick="copyOrdererInfo()">
                    <label class="form-check-label" for="sameAsOrderer">주문자 정보와 동일</label>
                </div>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label for="receiverName" class="form-label">수령인 이름 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="receiverName" name="receiverName" required>
                    </div>
                    <div class="col-md-6">
                        <label for="receiverTelno" class="form-label">수령인 연락처 <span class="text-danger">*</span></label>
                        <input type="tel" class="form-control" id="receiverTelno" name="receiverTelno" required>
                    </div>
                    <div class="col-md-4">
                        <label for="deliveryZipno" class="form-label">우편번호 <span class="text-danger">*</span></label>
                        <div class="input-group">
                            <input type="text" class="form-control" id="deliveryZipno" name="deliveryZipno" readonly required>
                            <button type="button" class="btn btn-secondary" onclick="findZipcode()">주소 찾기</button>
                        </div>
                    </div>
                    <div class="col-12">
                        <label for="deliveryAddress" class="form-label">배송 주소 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control mb-2" id="deliveryAddress" name="deliveryAddress" readonly required>
                        <input type="text" class="form-control" id="detailAddress" placeholder="상세주소" required>
                    </div>
                    <div class="col-12">
                        <label for="deliverySpace" class="form-label">배송 요청사항</label>
                        <select class="form-select mb-2" id="deliveryRequest" onchange="handleDeliveryRequest()">
                            <option value="">배송 요청사항을 선택해주세요</option>
                            <option value="door">문 앞에 놓아주세요</option>
                            <option value="security">경비실에 맡겨주세요</option>
                            <option value="call">배송 전 연락주세요</option>
                            <option value="custom">직접 입력</option>
                        </select>
                        <input type="text" class="form-control" id="deliverySpace" name="deliverySpace"
                               style="display: none;" placeholder="요청사항을 직접 입력해주세요">
                    </div>
                </div>
            </div>

            <!-- 결제 정보 -->
            <div class="payment-info mb-5">
                <h4 class="mb-3">결제 정보</h4>
                <div class="order-summary">
                    <div class="summary-item">
                        <span>상품 금액</span>
                        <span><fmt:formatNumber value="${totalOrderAmount}" pattern="#,###" />원</span>
                    </div>
                    <div class="summary-item">
                        <span>배송비</span>
                        <span><fmt:formatNumber value="${totalDeliveryFee}" pattern="#,###" />원</span>
                    </div>
                    <div class="summary-total">
                        <span>총 결제금액</span>
                        <span><fmt:formatNumber value="${totalOrderAmount + totalDeliveryFee}" pattern="#,###" />원</span>
                    </div>
                    <input type="hidden" name="deliveryFee" value="${totalDeliveryFee}">
                    <input type="hidden" name="deliveryPeriod" value="3"> <!-- 기본 배송 기간 3일 -->
                </div>
            </div>

            <!-- 결제하기 버튼 -->
            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-outline-secondary me-2" onclick="history.back()">이전으로</button>
                <button type="submit" class="btn btn-primary">결제하기</button>
            </div>
        </form>
    </div>
</div>

<!-- 부트스트랩 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>

<script>
    // 다음 우편번호 검색 API
    function findZipcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                document.getElementById('deliveryZipno').value = data.zonecode;
                document.getElementById('deliveryAddress').value = data.address;
                document.getElementById('detailAddress').focus();
            }
        }).open();
    }

    // 주문자 정보와 동일 체크박스 처리
    function copyOrdererInfo() {
        if (document.getElementById('sameAsOrderer').checked) {
            document.getElementById('receiverName').value = document.getElementById('orderPersonName').value;
            document.getElementById('receiverTelno').value = document.getElementById('orderPersonTelno').value;
        } else {
            document.getElementById('receiverName').value = '';
            document.getElementById('receiverTelno').value = '';
        }
    }

    // 배송 요청사항 처리
    function handleDeliveryRequest() {
        const select = document.getElementById('deliveryRequest');
        const customInput = document.getElementById('deliverySpace');

        if (select.value === 'custom') {
            customInput.style.display = 'block';
            customInput.required = true;
            customInput.value = '';
        } else {
            customInput.style.display = 'none';
            customInput.required = false;

            // 선택한 옵션 값을 input에 설정
            if (select.value === 'door') {
                customInput.value = '문 앞에 놓아주세요';
            } else if (select.value === 'security') {
                customInput.value = '경비실에 맡겨주세요';
            } else if (select.value === 'call') {
                customInput.value = '배송 전 연락주세요';
            } else {
                customInput.value = '';
            }
        }
    }

    // 폼 유효성 검사
    function validateForm() {
        const deliveryZipno = document.getElementById('deliveryZipno').value;
        const deliveryAddress = document.getElementById('deliveryAddress').value;
        const detailAddress = document.getElementById('detailAddress').value;

        if (!deliveryZipno || !deliveryAddress) {
            alert('주소를 입력해주세요.');
            return false;
        }

        if (!detailAddress) {
            alert('상세주소를 입력해주세요.');
            document.getElementById('detailAddress').focus();
            return false;
        }

        // 상세주소를 배송주소에 추가
        document.getElementById('deliveryAddress').value = deliveryAddress + ' ' + detailAddress;

        return true;
    }
</script>
</body>
</html>