package domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 상품 정보를 전송하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productCode;     // no_product - VARCHAR2(30), PK
    private String productName;     // nm_product - VARCHAR2(200)
    private String detailExplain;   // nm_detail_explain - CLOB
    private String fileId;          // id_file - VARCHAR2(30)
    private String startDate;       // dt_start_date - VARCHAR2(8)
    private String endDate;         // dt_end_date - VARCHAR2(8)
    private Integer customerPrice;  // qt_customer - NUMBER(9)
    private Integer salePrice;      // qt_sale_price - NUMBER(9)
    private Integer stock;          // qt_stock - NUMBER(9)
    private Integer deliveryFee;    // qt_delivery_fee - NUMBER(9)
    private String registerId;      // no_register - VARCHAR2(30)
    private Date firstDate;         // da_first_date - DATE
    private String status;

    
    /**
     * 유효성 검증 메서드
     */
    public boolean isValid() {
        // 필수 입력 항목 검증
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }
        
        // 판매가격은 필수이며 양수여야 함
        if (salePrice == null || salePrice <= 0) {
            return false;
        }
        
        return true;
    }

    /**
     * 상품의 상태를 판단하는 메서드
     * @return 상품 상태 문자열: "판매중", "품절", "판매중지"
     */
    public String getStatus() {
        // 현재 날짜 가져오기
        String currentDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());

        // 재고 확인
        if (stock != null && stock <= 0) {
            return "품절";
        }

        // 판매 기간 확인
        if (endDate != null && endDate.compareTo(currentDate) < 0) {
            return "판매중지";
        }

        // 위 조건 모두 해당하지 않으면 판매중
        return "판매중";
    }

}