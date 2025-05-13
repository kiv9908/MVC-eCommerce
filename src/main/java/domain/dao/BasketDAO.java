package domain.dao;

import domain.dto.BasketDTO;
import domain.dto.BasketItemDTO;

import java.util.List;

public interface BasketDAO {

    /**
     * 사용자 ID로 장바구니 조회
     */
    BasketDTO findBasketByUserId(String userId);

    /**
     * 새 장바구니 생성
     */
    boolean createBasket(BasketDTO basket);

    /**
     * 장바구니 항목 추가
     */
    boolean addBasketItem(BasketItemDTO item);

    /**
     * 장바구니 ID로 모든 항목 조회
     */
    List<BasketItemDTO> findBasketItemsByBasketId(Long basketId);

    /**
     * 장바구니와 상품 코드로 항목 조회
     */
    BasketItemDTO findBasketItemByProductCode(Long basketId, String productCode);

    /**
     * 장바구니 항목 수량 업데이트
     */
    boolean updateBasketItemQuantity(Long itemId, int quantity, int amount);

    /**
     * 장바구니 항목 가격 업데이트
     */
    boolean updateBasketItemPrice(Long itemId, int price, int amount);

    /**
     * 장바구니 항목 순번 업데이트
     */
    boolean updateBasketItemOrder(Long itemId, int order);

    /**
     * 장바구니 총액 업데이트
     */
    boolean updateBasketAmount(Long basketId, int totalAmount);

    /**
     * 장바구니 항목 삭제
     */
    boolean removeBasketItem(Long itemId);

    /**
     * 장바구니의 모든 항목 삭제
     */
    boolean clearBasket(Long basketId);

    /**
     * 장바구니에서 여러 항목 삭제
     */
    boolean removeMultipleBasketItems(Long basketId, List<Long> basketItemIds);
}