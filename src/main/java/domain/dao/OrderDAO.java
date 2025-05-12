package domain.dao;

import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.PageDTO;

import java.util.List;

/**
 * 주문 데이터 액세스 인터페이스
 */
public interface OrderDAO {
    /**
     * 주문 목록을 조회합니다.
     * @param pageDTO 페이징 정보
     * @return 주문 목록
     */
    List<OrderDTO> getOrderList(PageDTO pageDTO);

    /**
     * 주문의 총 개수를 조회합니다.
     * @return 총 주문 개수
     */
    int getTotalOrderCount();

    /**
     * ID로 주문을 조회합니다.
     * @param orderId 주문 ID
     * @return 주문 정보
     */
    OrderDTO getOrderById(String orderId);

    /**
     * 주문에 속한 모든 주문 항목을 조회합니다.
     * @param orderId 주문 ID
     * @return 주문 항목 목록
     */
    List<OrderItemDTO> getOrderItemsByOrderId(String orderId);

    /**
     * ID로 주문 항목을 조회합니다.
     * @param orderItemId 주문 항목 ID
     * @return 주문 항목 정보
     */
    OrderItemDTO getOrderItemById(String orderItemId);

    /**
     * 새 주문을 생성합니다.
     * @param orderDTO 주문 정보
     * @return 생성된 주문 ID
     */
    String createOrder(OrderDTO orderDTO);

    /**
     * 주문 항목을 생성합니다.
     * @param orderItemDTO 주문 항목 정보
     * @return 성공 시 1, 실패 시 0
     */
    int createOrderItem(OrderItemDTO orderItemDTO);

    /**
     * 주문 상태를 업데이트합니다.
     * @param orderId 주문 ID
     * @param orderStatus 주문 상태
     * @return 성공 시 1, 실패 시 0
     */
    int updateOrderStatus(String orderId, String orderStatus);

    /**
     * 결제 상태를 업데이트합니다.
     * @param orderId 주문 ID
     * @param paymentStatus 결제 상태
     * @return 성공 시 1, 실패 시 0
     */
    int updatePaymentStatus(String orderId, String paymentStatus);

    /**
     * 주문 정보를 업데이트합니다.
     * @param orderDTO 주문 정보
     * @return 성공 시 1, 실패 시 0
     */
    int updateOrder(OrderDTO orderDTO);

    /**
     * 주문 항목 정보를 업데이트합니다.
     * @param orderItemDTO 주문 항목 정보
     * @return 성공 시 1, 실패 시 0
     */
    int updateOrderItem(OrderItemDTO orderItemDTO);

    /**
     * 주문을 삭제합니다. (주문 항목도 함께 삭제)
     * @param orderId 주문 ID
     * @return 성공 시 1, 실패 시 0
     */
    int deleteOrder(String orderId);

    /**
     * 주문 항목을 삭제합니다.
     * @param orderItemId 주문 항목 ID
     * @return 성공 시 1, 실패 시 0
     */
    int deleteOrderItem(String orderItemId);

    /**
     * 사용자 ID로 주문 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param pageDTO 페이징 정보
     * @return 사용자의 주문 목록
     */
    List<OrderDTO> getOrdersByUserId(String userId, PageDTO pageDTO);

    /**
     * 사용자의 주문 총 개수를 조회합니다.
     * @param userId 사용자 ID
     * @return 사용자의 총 주문 개수
     */
    int getTotalOrderCountByUserId(String userId);
}