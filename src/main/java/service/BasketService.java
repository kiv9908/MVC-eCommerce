package service;

import domain.dao.BasketDAO;
import domain.dao.BasketDAOImpl;
import domain.dao.ProductDAO;
import domain.dto.BasketDTO;
import domain.dto.BasketItemDTO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BasketService {
    private final BasketDAO basketDAO;
    private final ProductDAO productDAO;

    public BasketService(BasketDAO basketDAO, ProductDAO productDAO) {
        this.basketDAO = basketDAO;
        this.productDAO = productDAO;
    }

    /**
     * 장바구니에 상품 추가
     */
    public boolean addToBasket(String userId, String productCode, int quantity) {
        try {
            // 상품 존재 여부 및 재고 확인
            ProductDTO product = productDAO.findByProductCode(productCode);
            if (product == null) {
                log.error("장바구니 추가 실패: 상품을 찾을 수 없음 (productCode={})", productCode);
                return false;
            }

            // 재고 체크
            if (product.getStock() != null && product.getStock() < quantity) {
                log.error("장바구니 추가 실패: 재고 부족 (요청: {}, 재고: {})", quantity, product.getStock());
                return false;
            }

            // 판매 상태 확인
            if (!"판매중".equals(product.getStatus())) {
                log.error("장바구니 추가 실패: 판매 중인 상품이 아님 (상태: {})", product.getStatus());
                return false;
            }

            // 사용자의 장바구니 찾기 또는 생성
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                // 장바구니 없으면 새로 생성
                basket = new BasketDTO();
                basket.setUserId(userId);
                basket.setTotalAmount(0);
                basket.setRegisterId(userId);

                boolean created = basketDAO.createBasket(basket);
                if (!created) {
                    log.error("장바구니 생성 실패: userId={}", userId);
                    return false;
                }

                // 생성 후 다시 조회하여 ID 획득
                basket = basketDAO.findBasketByUserId(userId);
                if (basket == null) {
                    log.error("생성된 장바구니를 찾을 수 없음: userId={}", userId);
                    return false;
                }
            }

            // 장바구니에 이미 상품이 있는지 확인
            BasketItemDTO existingItem = basketDAO.findBasketItemByProductCode(basket.getBasketId(), productCode);

            int salePrice = product.getSalePrice();

            if (existingItem != null) {
                // 이미 있으면 수량 증가
                int newQuantity = existingItem.getQuantity() + quantity;
                int newAmount = salePrice * newQuantity;

                boolean updated = basketDAO.updateBasketItemQuantity(existingItem.getItemId(), newQuantity, newAmount);
                if (!updated) {
                    log.error("장바구니 항목 수량 업데이트 실패: itemId={}", existingItem.getItemId());
                    return false;
                }
            } else {
                // 없으면 새로 추가
                BasketItemDTO newItem = new BasketItemDTO();
                newItem.setBasketId(basket.getBasketId());
                newItem.setProductCode(productCode);
                newItem.setUserId(userId);
                newItem.setPrice(salePrice);
                newItem.setQuantity(quantity);
                newItem.setAmount(salePrice * quantity);
                newItem.setRegisterId(userId);

                boolean added = basketDAO.addBasketItem(newItem);
                if (!added) {
                    log.error("장바구니 항목 추가 실패: productCode={}", productCode);
                    return false;
                }
            }

            // 장바구니 총액 갱신
            updateBasketTotalAmount(basket.getBasketId());

            return true;
        } catch (Exception e) {
            log.error("장바구니 추가 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 장바구니 총액 갱신
     */
    private boolean updateBasketTotalAmount(Long basketId) {
        try {
            // 장바구니 항목 조회
            List<BasketItemDTO> items = basketDAO.findBasketItemsByBasketId(basketId);

            // 총액 계산
            int totalAmount = 0;
            for (BasketItemDTO item : items) {
                totalAmount += item.getAmount();
            }

            // 장바구니 총액 업데이트
            return basketDAO.updateBasketAmount(basketId, totalAmount);
        } catch (Exception e) {
            log.error("장바구니 총액 업데이트 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 사용자의 장바구니 조회
     */
    public BasketDTO getOrCreateBasket(String userId) {
        try {
            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);

            // 장바구니가 없으면 새로 생성
            if (basket == null) {
                log.info("장바구니 없음, 새로 생성: userId={}", userId);

                basket = new BasketDTO();
                basket.setUserId(userId);
                basket.setTotalAmount(0);
                basket.setRegisterId(userId);

                boolean created = basketDAO.createBasket(basket);
                if (!created) {
                    log.error("장바구니 생성 실패: userId={}", userId);
                    return null;
                }

                // 생성 후 다시 조회하여 ID 획득
                basket = basketDAO.findBasketByUserId(userId);
                if (basket == null) {
                    log.error("생성된 장바구니를 찾을 수 없음: userId={}", userId);
                    return null;
                }
            }

            // 장바구니 항목 조회
            List<BasketItemDTO> items = basketDAO.findBasketItemsByBasketId(basket.getBasketId());
            basket.setItems(items);

            return basket;
        } catch (Exception e) {
            log.error("장바구니 조회/생성 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 장바구니 항목 수량 수정
     */
    public boolean updateBasketItemQuantity(Long basketItemId, String userId, int quantity) {
        try {
            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                log.error("장바구니를 찾을 수 없음: userId={}", userId);
                return false;
            }

            // 장바구니 항목 조회
            List<BasketItemDTO> items = basketDAO.findBasketItemsByBasketId(basket.getBasketId());
            BasketItemDTO itemToUpdate = null;

            // 수정할 항목 찾기
            for (BasketItemDTO item : items) {
                if (item.getItemId().equals(basketItemId)) {
                    itemToUpdate = item;
                    break;
                }
            }

            if (itemToUpdate == null) {
                log.error("수정할 장바구니 항목을 찾을 수 없음: basketItemId={}", basketItemId);
                return false;
            }

            // 수량 체크
            if (quantity <= 0) {
                log.error("유효하지 않은 수량: {}", quantity);
                return false;
            }

            // 상품 정보 조회
            ProductDTO product = productDAO.findByProductCode(itemToUpdate.getProductCode());
            if (product == null) {
                log.error("상품을 찾을 수 없음: productCode={}", itemToUpdate.getProductCode());
                return false;
            }

            // 재고 체크
            if (product.getStock() != null && product.getStock() < quantity) {
                log.error("재고 부족 (요청: {}, 재고: {})", quantity, product.getStock());
                return false;
            }

            // 판매 상태 확인
            if (!"판매중".equals(product.getStatus())) {
                log.error("판매 중인 상품이 아님 (상태: {})", product.getStatus());
                return false;
            }

            // 금액 계산
            int amount = itemToUpdate.getPrice() * quantity;

            // 수량 및 금액 업데이트
            boolean updated = basketDAO.updateBasketItemQuantity(basketItemId, quantity, amount);
            if (!updated) {
                log.error("장바구니 항목 수량 업데이트 실패: basketItemId={}", basketItemId);
                return false;
            }

            // 장바구니 총액 갱신
            updateBasketTotalAmount(basket.getBasketId());

            return true;
        } catch (Exception e) {
            log.error("장바구니 항목 수량 수정 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 장바구니 항목 삭제
     */
    public boolean removeBasketItem(Long basketItemId, String userId) {
        try {
            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                log.error("장바구니를 찾을 수 없음: userId={}", userId);
                return false;
            }

            // 항목 삭제
            boolean removed = basketDAO.removeBasketItem(basketItemId);
            if (!removed) {
                log.error("장바구니 항목 삭제 실패: basketItemId={}", basketItemId);
                return false;
            }

            // 장바구니 총액 갱신
            updateBasketTotalAmount(basket.getBasketId());

            return true;
        } catch (Exception e) {
            log.error("장바구니 항목 삭제 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 장바구니 비우기 (사용자의 모든 장바구니 항목 삭제)
     */
    public boolean clearBasket(String userId) {
        try {
            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                log.error("장바구니를 찾을 수 없음: userId={}", userId);
                return false;
            }

            // 장바구니의 모든 항목 삭제
            boolean cleared = basketDAO.clearBasket(basket.getBasketId());
            if (!cleared) {
                log.error("장바구니 비우기 실패: basketId={}", basket.getBasketId());
                return false;
            }

            // 장바구니 총액 0으로 업데이트
            basketDAO.updateBasketAmount(basket.getBasketId(), 0);

            return true;
        } catch (Exception e) {
            log.error("장바구니 비우기 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 선택한 장바구니 항목들 삭제
     */
    public boolean removeSelectedItems(String userId, List<Long> itemIds) {
        try {
            if (itemIds == null || itemIds.isEmpty()) {
                log.error("삭제할 항목이 없음");
                return false;
            }

            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                log.error("장바구니를 찾을 수 없음: userId={}", userId);
                return false;
            }

            // 다중 항목 삭제
            boolean removed = basketDAO.removeMultipleBasketItems(basket.getBasketId(), itemIds);
            if (!removed) {
                log.error("선택한 장바구니 항목 삭제 실패: basketId={}, itemIds={}", basket.getBasketId(), itemIds);
                return false;
            }

            // 장바구니 총액 갱신
            updateBasketTotalAmount(basket.getBasketId());

            return true;
        } catch (Exception e) {
            log.error("선택한 장바구니 항목 삭제 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 장바구니 상품 정보 갱신 (재고, 가격, 상태 등이 변경되었을 때)
     */
    public boolean refreshBasketItems(String userId) {
        try {
            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                log.error("장바구니를 찾을 수 없음: userId={}", userId);
                return false;
            }

            // 장바구니 항목 조회
            List<BasketItemDTO> items = basketDAO.findBasketItemsByBasketId(basket.getBasketId());
            boolean needsUpdate = false;

            for (BasketItemDTO item : items) {
                // 상품 정보 조회
                ProductDTO product = productDAO.findByProductCode(item.getProductCode());

                // 상품이 없거나 판매 중이 아닌 경우 해당 항목 삭제
                if (product == null || !"판매중".equals(product.getStatus())) {
                    basketDAO.removeBasketItem(item.getItemId());
                    needsUpdate = true;
                    continue;
                }

                // 가격이 변경된 경우 업데이트
                int currentPrice = product.getSalePrice();
                if (item.getPrice() != currentPrice) {
                    int amount = currentPrice * item.getQuantity();
                    basketDAO.updateBasketItemPrice(item.getItemId(), currentPrice, amount);
                    needsUpdate = true;
                }

                // 재고가 수량보다 적은 경우 수량 조정
                if (product.getStock() != null && product.getStock() < item.getQuantity()) {
                    int newQuantity = product.getStock();
                    int amount = currentPrice * newQuantity;
                    basketDAO.updateBasketItemQuantity(item.getItemId(), newQuantity, amount);
                    needsUpdate = true;
                }
            }

            // 필요한 경우 장바구니 총액 갱신
            if (needsUpdate) {
                updateBasketTotalAmount(basket.getBasketId());
            }

            return true;
        } catch (Exception e) {
            log.error("장바구니 항목 갱신 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 선택된 장바구니 항목들의 수량 업데이트
     */
    public boolean updateSelectedItemsQuantity(String userId, List<Long> itemIds, List<Integer> quantities) {
        try {
            if (itemIds == null || quantities == null || itemIds.size() != quantities.size() || itemIds.isEmpty()) {
                log.error("유효하지 않은 입력: itemIds={}, quantities={}", itemIds, quantities);
                return false;
            }

            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                log.error("장바구니를 찾을 수 없음: userId={}", userId);
                return false;
            }

            // 각 항목 업데이트
            boolean allUpdated = true;
            for (int i = 0; i < itemIds.size(); i++) {
                Long itemId = itemIds.get(i);
                int quantity = quantities.get(i);

                // 수량 체크
                if (quantity <= 0) {
                    log.error("유효하지 않은 수량: {}", quantity);
                    allUpdated = false;
                    continue;
                }

                // 해당 항목 찾기
                BasketItemDTO itemToUpdate = null;
                List<BasketItemDTO> items = basketDAO.findBasketItemsByBasketId(basket.getBasketId());
                for (BasketItemDTO item : items) {
                    if (item.getItemId().equals(itemId)) {
                        itemToUpdate = item;
                        break;
                    }
                }

                if (itemToUpdate == null) {
                    log.error("장바구니 항목을 찾을 수 없음: itemId={}", itemId);
                    allUpdated = false;
                    continue;
                }

                // 상품 정보 조회
                ProductDTO product = productDAO.findByProductCode(itemToUpdate.getProductCode());
                if (product == null) {
                    log.error("상품을 찾을 수 없음: productCode={}", itemToUpdate.getProductCode());
                    allUpdated = false;
                    continue;
                }

                // 재고 체크
                if (product.getStock() != null && product.getStock() < quantity) {
                    log.error("재고 부족 (요청: {}, 재고: {})", quantity, product.getStock());
                    allUpdated = false;
                    continue;
                }

                // 금액 계산
                int amount = itemToUpdate.getPrice() * quantity;

                // 수량 및 금액 업데이트
                boolean updated = basketDAO.updateBasketItemQuantity(itemId, quantity, amount);
                if (!updated) {
                    log.error("장바구니 항목 수량 업데이트 실패: itemId={}", itemId);
                    allUpdated = false;
                }
            }

            // 장바구니 총액 갱신
            updateBasketTotalAmount(basket.getBasketId());

            return allUpdated;
        } catch (Exception e) {
            log.error("장바구니 항목 수량 일괄 업데이트 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 장바구니 내 상품 존재 확인
     */
    public boolean isProductInBasket(String userId, String productCode) {
        try {
            // 사용자의 장바구니 조회
            BasketDTO basket = basketDAO.findBasketByUserId(userId);
            if (basket == null) {
                return false;
            }

            // 장바구니에 상품 확인
            BasketItemDTO item = basketDAO.findBasketItemByProductCode(basket.getBasketId(), productCode);
            return item != null;
        } catch (Exception e) {
            log.error("장바구니 상품 확인 중 오류 발생", e);
            return false;
        }
    }
}