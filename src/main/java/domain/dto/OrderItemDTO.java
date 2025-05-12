package domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 주문 품목 정보를 전송하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String orderItemId;        // id_order_item - VARCHAR2(30), PK
    private String orderId;            // id_order - VARCHAR2(30), FK
    private Integer orderItemCount;    // cn_order_item - NUMBER(5)
    private String productCode;        // no_product - VARCHAR2(30), FK
    private String userId;             // no_user - VARCHAR2(30), FK
    private Integer unitPrice;         // qt_unit_price - NUMBER(9)
    private Integer quantity;          // qt_order_item - NUMBER(9)
    private Integer amount;            // qt_order_item_amount - NUMBER(9)
    private Integer deliveryFee;       // qt_order_item_delivery_fee - NUMBER(9)
    private String paymentStatus;      // st_payment - VARCHAR2(4)
    private String registerId;         // no_register - VARCHAR2(30)
    private Date firstDate;            // da_first_date - DATE

    // 화면 표시용 추가 필드 (실제 DB 테이블에는 없음)
    private String productName;        // 상품명
    private String fileId;             // 상품 이미지 파일 ID

    /**
     * 유효성 검증 메서드
     */
    public boolean isValid() {
        // 필수 입력 항목 검증
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }

        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }

        // 수량은 필수이며 양수여야 함
        if (quantity == null || quantity <= 0) {
            return false;
        }

        // 단가는 필수이며 양수여야 함
        if (unitPrice == null || unitPrice < 0) {
            return false;
        }

        return true;
    }

    /**
     * 주문 항목의 총 금액을 계산
     * @return 총 금액 (상품 금액 + 배송비)
     */
    public int getTotalAmount() {
        int itemAmount = (amount != null) ? amount : 0;
        int shipFee = (deliveryFee != null) ? deliveryFee : 0;
        return itemAmount + shipFee;
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