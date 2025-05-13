package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasketDTO {
    private Long basketId;           // nb_basket
    private String userId;           // no_user
    private Integer totalAmount;     // qt_basket_amount
    private String registerId;       // no_register
    private Date createdDate;        // da_first_date

    // 장바구니 품목 리스트
    private List<BasketItemDTO> items;
}