package command.user.order;

import command.Command;
import domain.dao.BasketDAOImpl;
import domain.dao.ProductDAOImpl;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.BasketService;
import service.OrderService;
import config.AppConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 주문을 생성하는 Command 클래스
 * URL: /user/order/create.do
 */
@Slf4j
public class OrderCreateCommand implements Command {

    private OrderService orderService;
    private BasketService basketService;

    // 주문 상태 및 결제 상태 코드
    private static final String ORDER_STATUS_COMPLETE = "10"; // 주문완료
    private static final String PAYMENT_STATUS_COMPLETE = "20"; // 결제완료

    public OrderCreateCommand() {
        // 서비스 초기화
        this.orderService = new OrderService();
        // BasketService 초기화
        BasketDAOImpl basketDAO = new BasketDAOImpl();
        ProductDAOImpl productDAO = new ProductDAOImpl();
        this.basketService = new BasketService(basketDAO, productDAO);    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("CreateOrderCommand 실행");

        try {
            // 세션에서 로그인 사용자 정보 확인
            HttpSession session = request.getSession();
            UserDTO userFromSession = (UserDTO) session.getAttribute("user");

            if (userFromSession == null || userFromSession.getUserId() == null) {
                // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
                return "redirect:/user/login";
            }

            String userId = userFromSession.getUserId();

            // 세션에서 주문 항목 정보 복원
            Object sessionItems = session.getAttribute("orderItems");
            if (sessionItems == null) {
                request.setAttribute("errorMessage", "주문 정보가 유효하지 않습니다. 다시 시도해주세요.");
                return "/WEB-INF/views/common/error.jsp";
            }

            List<OrderItemDTO> orderItems = (List<OrderItemDTO>) sessionItems;
            log.info("세션에서 주문 항목 복원: {} 개", orderItems.size());

            // 총 금액 정보도 복원
            int totalOrderAmount = 0;
            int totalDeliveryFee = 0;

            Object sessionAmount = session.getAttribute("totalOrderAmount");
            if (sessionAmount != null) {
                totalOrderAmount = (Integer) sessionAmount;
            }

            Object sessionFee = session.getAttribute("totalDeliveryFee");
            if (sessionFee != null) {
                totalDeliveryFee = (Integer) sessionFee;
            }

            // 주문 항목 로그 출력
            for (OrderItemDTO item : orderItems) {
                log.info("주문 항목: 상품코드={}, 상품명={}, 수량={}",
                        item.getProductCode(), item.getProductName(), item.getQuantity());
            }

            // 주문 생성
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setUserId(userId);
            orderDTO.setOrderAmount(totalOrderAmount);
            orderDTO.setDeliveryFee(totalDeliveryFee);

            // 배송 정보 설정
            orderDTO.setOrderPersonName(request.getParameter("orderPersonName"));
            orderDTO.setReceiverName(request.getParameter("receiverName"));
            orderDTO.setDeliveryZipno(request.getParameter("deliveryZipno"));
            orderDTO.setDeliveryAddress(request.getParameter("deliveryAddress"));
            orderDTO.setReceiverTelno(request.getParameter("receiverTelno"));
            orderDTO.setDeliverySpace(request.getParameter("deliverySpace"));
            orderDTO.setDeliveryPeriod(3); // 기본 3일

            // 주문 상태 설정 (상태 코드 사용)
            orderDTO.setOrderType("10"); // 일반주문 (기본값)
            orderDTO.setOrderStatus(ORDER_STATUS_COMPLETE); // 주문완료 상태
            orderDTO.setPaymentStatus(PAYMENT_STATUS_COMPLETE); // 결제완료 상태
            orderDTO.setRegisterId(userId);
            orderDTO.setOrderDate(new Date());

            // 필수 정보 검증
            if (orderDTO.getOrderPersonName() == null || orderDTO.getReceiverName() == null ||
                    orderDTO.getDeliveryAddress() == null || orderDTO.getReceiverTelno() == null) {
                request.setAttribute("errorMessage", "주문에 필요한 정보가 누락되었습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 생성
            String orderId = orderService.createOrder(orderDTO);

            if (orderId == null) {
                request.setAttribute("errorMessage", "주문 생성 중 오류가 발생했습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 항목 저장
            for (OrderItemDTO item : orderItems) {
                log.info("주문 항목 생성 시도: {}", item.getProductCode());
                item.setOrderId(orderId);
                item.setPaymentStatus(PAYMENT_STATUS_COMPLETE); // 결제완료 상태
                item.setRegisterId(userId);

                boolean result = orderService.createOrderItem(item);
                log.info("주문 항목 생성 결과: {}", result);

                if (!result) {
                    log.error("주문 항목 생성 실패: {}", item.getProductCode());
                }
            }

            // 주문 금액 업데이트 (실제 주문 항목 기준)
            orderService.updateOrderAmount(orderId);

            // 장바구니에서 주문한 경우, 주문한 상품 장바구니에서 제거
            if (request.getParameterValues("itemId") != null) {
                String[] itemIdsArray = request.getParameterValues("itemId");
                List<Long> itemIdList = new ArrayList<>();

                for (String itemIdStr : itemIdsArray) {
                    try {
                        itemIdList.add(Long.parseLong(itemIdStr));
                    } catch (NumberFormatException e) {
                        log.warn("장바구니 항목 ID 변환 중 오류: {}", e.getMessage());
                    }
                }

                if (!itemIdList.isEmpty()) {
                    basketService.removeSelectedItems(userId, itemIdList);
                }
            }

            // 세션에서 주문 항목 정보 제거 (주문 완료 후)
            session.removeAttribute("orderItems");
            session.removeAttribute("totalOrderAmount");
            session.removeAttribute("totalDeliveryFee");

            // 주문 완료 페이지로 이동
            request.setAttribute("orderId", orderId);
            basketService.clearBasket(userId);
            return "/WEB-INF/views/user/orderComplete.jsp";

        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}