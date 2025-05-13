package command.user.order;

import command.Command;
import config.AppConfig;
import domain.dao.BasketDAOImpl;
import domain.dao.ProductDAOImpl;
import domain.dto.BasketDTO;
import domain.dto.BasketItemDTO;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.ProductDTO;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.BasketService;
import service.OrderService;
import service.ProductService;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 사용자 주문 생성을 처리하는 Command 클래스
 */
@Slf4j
public class OrderCreateCommand implements Command {

    private OrderService orderService;
    private ProductService productService;
    private BasketService basketService;
    private UserService userService;

    // 주문 상태 및 결제 상태 코드
    private static final String ORDER_STATUS_COMPLETE = "10"; // 주문완료
    private static final String PAYMENT_STATUS_COMPLETE = "20"; // 결제완료

    public OrderCreateCommand() {
        // AppConfig를 통한 서비스 초기화
        this.orderService = new OrderService();
        this.productService = AppConfig.getInstance().getProductService();
        this.userService = AppConfig.getInstance().getUserService();

        // BasketService 초기화 (AppConfig에 없어서 직접 생성)
        BasketDAOImpl basketDAO = new BasketDAOImpl();
        ProductDAOImpl productDAO = new ProductDAOImpl();
        this.basketService = new BasketService(basketDAO, productDAO);
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 세션에서 로그인 사용자 정보 확인
            HttpSession session = request.getSession();
            UserDTO userFromSession = (UserDTO) session.getAttribute("user");
            String userId = userFromSession.getUserId();

            if (userId == null) {
                // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
                return "redirect:/user/login";
            }

            // GET 요청: 주문 양식 페이지로 이동
            if (request.getMethod().equalsIgnoreCase("GET")) {
                // 상태 코드 리스트 전달
                request.setAttribute("orderStatusList", getOrderStatusList());
                request.setAttribute("paymentStatusList", getPaymentStatusList());
                request.setAttribute("user", userFromSession);
                return "/WEB-INF/views/user/order/orderForm.jsp";
            }

            // POST 요청 처리
            List<OrderItemDTO> orderItems = new ArrayList<>();
            int totalOrderAmount = 0;
            int totalDeliveryFee = 0;

            // 상품 상세 페이지에서 넘어온 경우 (단일 상품)
            if (request.getParameter("productCode") != null) {
                String productCode = request.getParameter("productCode");
                int quantity = 1;

                try {
                    quantity = Integer.parseInt(request.getParameter("quantity"));
                } catch (NumberFormatException e) {
                    log.warn("수량 변환 중 오류: {}", e.getMessage());
                }

                // 상품 정보 조회
                ProductDTO product = productService.getProductDTOByCode(productCode);
                if (product != null) {
                    OrderItemDTO orderItem = new OrderItemDTO();
                    orderItem.setOrderItemCount(1);
                    orderItem.setProductCode(productCode);
                    orderItem.setQuantity(quantity);
                    orderItem.setUnitPrice(product.getSalePrice());
                    orderItem.setDeliveryFee(product.getDeliveryFee());
                    orderItem.setAmount(product.getSalePrice() * quantity);
                    orderItem.setUserId(userId);
                    orderItem.setProductName(product.getProductName());
                    orderItem.setFileId(product.getFileId());

                    totalOrderAmount += orderItem.getAmount();
                    totalDeliveryFee += product.getDeliveryFee();

                    orderItems.add(orderItem);
                }
            }
            // 장바구니에서 넘어온 경우 (선택된 상품들)
            else if (request.getParameterValues("itemId") != null) {
                String[] itemIdsArray = request.getParameterValues("itemId");
                List<Long> itemIds = new ArrayList<>();

                // String 배열을 Long 리스트로 변환
                for (String itemIdStr : itemIdsArray) {
                    try {
                        itemIds.add(Long.parseLong(itemIdStr));
                    } catch (NumberFormatException e) {
                        log.warn("장바구니 항목 ID 변환 중 오류: {}", e.getMessage());
                    }
                }

                // 장바구니 조회
                BasketDTO basket = basketService.getOrCreateBasket(userId);
                if (basket != null && basket.getItems() != null) {
                    int itemCount = 1;

                    // 선택된 장바구니 항목만 처리
                    for (Long itemId : itemIds) {
                        // 해당 장바구니 항목 찾기
                        BasketItemDTO basketItem = null;
                        for (BasketItemDTO item : basket.getItems()) {
                            if (item.getItemId().equals(itemId)) {
                                basketItem = item;
                                break;
                            }
                        }

                        if (basketItem != null) {
                            // 상품 정보 조회 (재고 및 배송비 확인)
                            ProductDTO product = productService.getProductDTOByCode(basketItem.getProductCode());
                            if (product != null) {
                                // 주문 항목 생성
                                OrderItemDTO orderItem = new OrderItemDTO();
                                orderItem.setOrderItemCount(itemCount++);
                                orderItem.setProductCode(basketItem.getProductCode());
                                orderItem.setQuantity(basketItem.getQuantity());
                                orderItem.setUnitPrice(basketItem.getPrice());
                                orderItem.setDeliveryFee(product.getDeliveryFee());
                                orderItem.setAmount(basketItem.getAmount());
                                orderItem.setUserId(userId);
                                orderItem.setProductName(product.getProductName());
                                orderItem.setFileId(product.getFileId());

                                totalOrderAmount += basketItem.getAmount();
                                totalDeliveryFee += product.getDeliveryFee();

                                orderItems.add(orderItem);
                            }
                        }
                    }
                }
            }

            // 주문 항목이 없는 경우
            if (orderItems.isEmpty()) {
                request.setAttribute("errorMessage", "주문할 상품이 없습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 양식 정보가 전달되었는지 확인 (최종 주문 처리)
            if (request.getParameter("orderPersonName") != null) {
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

                // 주문 생성
                String orderId = orderService.createOrder(orderDTO);

                if (orderId == null) {
                    request.setAttribute("errorMessage", "주문 생성 중 오류가 발생했습니다.");
                    return "/WEB-INF/views/common/error.jsp";
                }

                // 주문 항목 저장
                for (OrderItemDTO item : orderItems) {
                    item.setOrderId(orderId);
                    item.setPaymentStatus(PAYMENT_STATUS_COMPLETE); // 결제완료 상태
                    item.setRegisterId(userId);

                    boolean result = orderService.createOrderItem(item);
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

                // 주문 완료 페이지로 이동
                request.setAttribute("orderId", orderId);
                basketService.clearBasket(userId);
                return "/WEB-INF/views/user/orderComplete.jsp";
            }
            else {
                // 주문 양식 페이지로 이동 (주문 정보 전달)
                request.setAttribute("orderItems", orderItems);
                request.setAttribute("totalOrderAmount", totalOrderAmount);
                request.setAttribute("totalDeliveryFee", totalDeliveryFee);

                // 상태 코드 리스트 전달
                request.setAttribute("orderStatusList", getOrderStatusList());
                request.setAttribute("paymentStatusList", getPaymentStatusList());

                // 사용자 정보 조회
                UserDTO user = userService.getUserByEmail(userId);
                request.setAttribute("user", user);

                return "/WEB-INF/views/user/orderForm.jsp";
            }

        } catch (Exception e) {
            log.error("주문 처리 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/common/error.jsp";
        }
    }

    /**
     * 주문 상태 코드 리스트 생성
     * @return 주문 상태 코드 맵
     */
    private List<String[]> getOrderStatusList() {
        List<String[]> statusList = new ArrayList<>();
        statusList.add(new String[]{"10", "주문완료"});
        statusList.add(new String[]{"30", "배송 전"});
        statusList.add(new String[]{"40", "배송 중"});
        statusList.add(new String[]{"50", "배송 완료"});
        return statusList;
    }

    /**
     * 결제 상태 코드 리스트 생성
     * @return 결제 상태 코드 맵
     */
    private List<String[]> getPaymentStatusList() {
        List<String[]> statusList = new ArrayList<>();
        statusList.add(new String[]{"20", "결제완료"});
        return statusList;
    }
}