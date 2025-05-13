package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemDTO {
    private Long itemId;           // nb_basket_item
    private Long basketId;         // nb_basket
    private Integer itemOrder;     // cn_basket_item_order
    private String productCode;    // no_product
    private String userId;         // no_user
    private Integer price;         // qt_basket_item_price
    private Integer quantity;      // qt_basket_item
    private Integer amount;        // qt_basket_item_amount
    private String registerId;     // no_register
    private Date createdDate;      // da_first_date

    // 상품 정보를 함께 표시하기 위한 추가 필드들 (조인 결과를 담기 위함)
    private String productName;
    private Integer customerPrice;
    private String fileId;
    private Integer stock;
    private Integer deliveryFee;
    private String status;

    // 체크박스 선택 여부를 위한 필드 (UI용)
    private boolean selected = true;
}