package domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 주문 정보를 전송하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String orderId;            // id_order - VARCHAR2(30), PK
    private String userId;             // id_user - VARCHAR2(30), FK
    private Integer orderAmount;       // qt_order_amount - NUMBER(9)
    private Integer deliveryFee;       // qt_deli_money - NUMBER(9)
    private Integer deliveryPeriod;    // qt_deli_period - NUMBER(9)
    private String orderPersonName;    // nm_order_person - VARCHAR2(100)
    private String receiverName;       // nm_receiver - VARCHAR2(100)
    private String deliveryZipno;      // no_delivery_zipno - VARCHAR2(20)
    private String deliveryAddress;    // nm_delivery_address - VARCHAR2(200)
    private String receiverTelno;      // nm_receiver_telno - VARCHAR2(20)
    private String deliverySpace;      // nm_delivery_space - VARCHAR2(100)
    private String orderType;          // cd_order_type - VARCHAR2(4)
    private Date orderDate;            // da_order - DATE
    private String orderStatus;        // st_order - VARCHAR2(4)
    private String paymentStatus;      // st_payment - VARCHAR2(4)
    private String registerId;         // no_register - VARCHAR2(30)
    private Date firstDate;            // da_first_date - DATE

    // 화면 표시용 필드 (실제 DB 테이블에는 없음)
    private int totalItemCount;        // 주문 상품 개수

    /**
     * 유효성 검증 메서드
     */
    public boolean isValid() {
        // 필수 입력 항목 검증
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }

        // 주문 금액은 필수이며 양수여야 함
        if (orderAmount == null || orderAmount <= 0) {
            return false;
        }

        // 수령인 정보 필수
        if (receiverName == null || receiverName.trim().isEmpty() ||
                receiverTelno == null || receiverTelno.trim().isEmpty() ||
                deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 주문 상태 코드를 사람이 읽을 수 있는 문자열로 변환
     * @return 주문 상태 문자열
     */
    public String getOrderStatusText() {
        if (orderStatus == null) {
            return "알 수 없음";
        }

        switch (orderStatus) {
            case "ORD1": return "결제완료";
            case "ORD2": return "상품준비중";
            case "ORD3": return "배송중";
            case "ORD4": return "배송완료";
            case "ORD5": return "취소";
            case "ORD6": return "환불";
            default: return "알 수 없음";
        }
    }

    /**
     * 결제 상태 코드를 사람이 읽을 수 있는 문자열로 변환
     * @return 결제 상태 문자열
     */
    public String getPaymentStatusText() {
        if (paymentStatus == null) {
            return "알 수 없음";
        }

        switch (paymentStatus) {
            case "PAY1": return "결제대기";
            case "PAY2": return "결제완료";
            case "PAY3": return "결제취소";
            default: return "알 수 없음";
        }
    }
}