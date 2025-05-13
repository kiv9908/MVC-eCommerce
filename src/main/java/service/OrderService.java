package service;

import config.AppConfig;
import domain.dao.OrderDAO;
import domain.dao.OrderDAOImpl;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.PageDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
public class OrderService {

    private OrderDAO orderDAO;
    private ProductService productService;

    /**
     * 생성자: OrderDAO 구현체 초기화
     */
    public OrderService() {
        this.orderDAO = new OrderDAOImpl();
        this.productService = AppConfig.getInstance().getProductService();
    }

    /**
     * 페이징 처리된 주문 목록을 조회합니다.
     * @param pageDTO 페이징 정보
     * @return 주문 목록
     */
    public List<OrderDTO> getOrderList(PageDTO pageDTO) {
        try {
            // 전체 주문 수 조회
            int totalCount = orderDAO.getTotalOrderCount();
            pageDTO.setTotalCount(totalCount);

            // 페이지네이션 계산
            pageDTO.calculatePagination();

            // 주문 목록 조회
            return orderDAO.getOrderList(pageDTO);
        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 특정 사용자의 페이징 처리된 주문 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param pageDTO 페이징 정보
     * @return 사용자의 주문 목록
     */
    public List<OrderDTO> getOrdersByUserId(String userId, PageDTO pageDTO) {
        try {
            // 사용자의 전체 주문 수 조회
            int totalCount = orderDAO.getTotalOrderCountByUserId(userId);
            pageDTO.setTotalCount(totalCount);

            // 페이지네이션 계산
            pageDTO.calculatePagination();

            // 사용자의 주문 목록 조회
            return orderDAO.getOrdersByUserId(userId, pageDTO);
        } catch (Exception e) {
            log.error("사용자별 주문 목록 조회 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 주문 상세 정보를 조회합니다.
     * @param orderId 주문 ID
     * @return 주문 상세 정보
     */
    public OrderDTO getOrderDetail(String orderId) {
        try {
            return orderDAO.getOrderById(orderId);
        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 주문에 속한 모든 주문 항목을 조회합니다.
     * @param orderId 주문 ID
     * @return 주문 항목 목록
     */
    public List<OrderItemDTO> getOrderItems(String orderId) {
        try {
            return orderDAO.getOrderItemsByOrderId(orderId);
        } catch (Exception e) {
            log.error("주문 항목 목록 조회 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 주문 항목 상세 정보를 조회합니다.
     * @param orderItemId 주문 항목 ID
     * @return 주문 항목 상세 정보
     */
    public OrderItemDTO getOrderItemDetail(String orderItemId) {
        try {
            return orderDAO.getOrderItemById(orderItemId);
        } catch (Exception e) {
            log.error("주문 항목 상세 조회 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 새 주문을 생성합니다.
     * @param orderDTO 주문 정보
     * @return 생성된 주문 ID
     */
    public String createOrder(OrderDTO orderDTO) {
        try {
            // 주문 정보 유효성 검증
            if (!orderDTO.isValid()) {
                log.warn("유효하지 않은 주문 정보");
                return null;
            }

            return orderDAO.createOrder(orderDTO);
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 주문 항목을 생성합니다.
     * @param orderItemDTO 주문 항목 정보
     * @return 성공 여부
     */
    public boolean createOrderItem(OrderItemDTO orderItemDTO) {
        try {
            // 주문 항목 정보 유효성 검증
            if (!orderItemDTO.isValid()) {
                log.warn("유효하지 않은 주문 항목 정보");
                return false;
            }

            int result = orderDAO.createOrderItem(orderItemDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 항목 생성 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 주문 상태를 변경합니다.
     * @param orderId 주문 ID
     * @param newStatus 새 주문 상태
     * @return 성공 여부
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        try {
            // 상태 코드 유효성 검증
            if (newStatus == null || newStatus.trim().isEmpty()) {
                log.warn("유효하지 않은 주문 상태 코드: " + newStatus);
                return false;
            }

            int result = orderDAO.updateOrderStatus(orderId, newStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 상태 변경 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 결제 상태를 변경합니다.
     * @param orderId 주문 ID
     * @param newStatus 새 결제 상태
     * @return 성공 여부
     */
    public boolean updatePaymentStatus(String orderId, String newStatus) {
        try {
            // 상태 코드 유효성 검증
            if (newStatus == null || newStatus.trim().isEmpty()) {
                log.warn("유효하지 않은 결제 상태 코드: " + newStatus);
                return false;
            }

            int result = orderDAO.updatePaymentStatus(orderId, newStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("결제 상태 변경 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 주문 정보를 업데이트합니다.
     * @param orderDTO 주문 정보
     * @return 성공 여부
     */
    public boolean updateOrder(OrderDTO orderDTO) {
        try {
            // 주문 정보 유효성 검증
            if (!orderDTO.isValid()) {
                log.warn("유효하지 않은 주문 정보");
                return false;
            }

            int result = orderDAO.updateOrder(orderDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 업데이트 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 주문 항목 정보를 업데이트합니다.
     * @param orderItemDTO 주문 항목 정보
     * @return 성공 여부
     */
    public boolean updateOrderItem(OrderItemDTO orderItemDTO) {
        try {
            // 주문 항목 정보 유효성 검증
            if (!orderItemDTO.isValid()) {
                log.warn("유효하지 않은 주문 항목 정보");
                return false;
            }

            int result = orderDAO.updateOrderItem(orderItemDTO);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 항목 업데이트 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 주문을 삭제합니다.
     * @param orderId 주문 ID
     * @return 성공 여부
     */
    public boolean deleteOrder(String orderId) {
        try {
            int result = orderDAO.deleteOrder(orderId);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 삭제 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 주문 항목을 삭제합니다.
     * @param orderItemId 주문 항목 ID
     * @return 성공 여부
     */
    public boolean deleteOrderItem(String orderItemId) {
        try {
            int result = orderDAO.deleteOrderItem(orderItemId);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 항목 삭제 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 주문 항목과 함께 주문 전체 상세 정보를 조회합니다.
     * @param orderId 주문 ID
     * @return 주문 상세 정보와 주문 항목 목록이 채워진 OrderDTO
     */
    public OrderDTO getCompleteOrderDetail(String orderId) {
        try {
            // 주문 기본 정보 조회
            OrderDTO orderDTO = orderDAO.getOrderById(orderId);

            if (orderDTO != null) {
                // 주문 항목 목록 조회 및 설정
                List<OrderItemDTO> orderItems = orderDAO.getOrderItemsByOrderId(orderId);
                // 주문 항목 목록은 DTO에 직접 설정할 수 없으므로 request 객체에 별도로 저장
                // 따라서 여기서는 별도의 처리 없이 orderItems만 반환

                // 주문 총 항목 수 설정
                orderDTO.setTotalItemCount(orderItems.size());
            }

            return orderDTO;
        } catch (Exception e) {
            log.error("주문 전체 상세 조회 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 주문의 총 금액을 계산합니다.
     * @param orderId 주문 ID
     * @return 주문의 총 금액 (상품 금액 + 배송비)
     */
    public int calculateOrderTotal(String orderId) {
        try {
            List<OrderItemDTO> orderItems = orderDAO.getOrderItemsByOrderId(orderId);

            int total = 0;
            for (OrderItemDTO item : orderItems) {
                total += item.getTotalAmount(); // 상품 금액 + 배송비
            }

            return total;
        } catch (Exception e) {
            log.error("주문 총 금액 계산 중 오류 발생: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 주문 생성 후 주문 금액을 업데이트합니다.
     * @param orderId 주문 ID
     * @return 성공 여부
     */
    public boolean updateOrderAmount(String orderId) {
        try {
            // 주문 정보 조회
            OrderDTO orderDTO = orderDAO.getOrderById(orderId);

            if (orderDTO != null) {
                // 주문 항목들의 총 금액 계산
                int total = calculateOrderTotal(orderId);

                // 주문 금액 업데이트
                orderDTO.setOrderAmount(total);

                int result = orderDAO.updateOrder(orderDTO);
                return result > 0;
            }

            return false;
        } catch (Exception e) {
            log.error("주문 금액 업데이트 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

}