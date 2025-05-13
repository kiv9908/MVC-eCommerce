package command.user.order;

import command.Command;
import config.AppConfig;
import domain.dao.BasketDAOImpl;
import domain.dao.ProductDAOImpl;
import domain.dto.BasketDTO;
import domain.dto.BasketItemDTO;
import domain.dto.OrderItemDTO;
import domain.dto.ProductDTO;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.BasketService;
import service.ProductService;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 양식을 보여주는 Command 클래스
 * URL: /user/order/form.do
 */
@Slf4j
public class ShowOrderFormCommand implements Command {

    private ProductService productService;
    private BasketService basketService;
    private UserService userService;

    public ShowOrderFormCommand() {
        // 서비스 초기화
        this.productService = AppConfig.getInstance().getProductService();
        this.userService = AppConfig.getInstance().getUserService();

        // BasketService 초기화
        BasketDAOImpl basketDAO = new BasketDAOImpl();
        ProductDAOImpl productDAO = new ProductDAOImpl();
        this.basketService = new BasketService(basketDAO, productDAO);
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("ShowOrderFormCommand 실행");

        try {
            // 세션에서 로그인 사용자 정보 확인
            HttpSession session = request.getSession();
            UserDTO userFromSession = (UserDTO) session.getAttribute("user");

            if (userFromSession == null || userFromSession.getUserId() == null) {
                // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
                return "redirect:/user/login";
            }

            String userId = userFromSession.getUserId();

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
                log.info("선택된 장바구니 항목 수: {}", itemIdsArray.length);

                List<Long> itemIds = new ArrayList<>();

                // String 배열을 Long 리스트로 변환
                for (String itemIdStr : itemIdsArray) {
                    try {
                        itemIds.add(Long.parseLong(itemIdStr));
                        log.info("선택된 항목 ID: {}", itemIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("장바구니 항목 ID 변환 중 오류: {}", e.getMessage());
                    }
                }

                // 장바구니 조회
                BasketDTO basket = basketService.getOrCreateBasket(userId);
                if (basket != null && basket.getItems() != null) {
                    log.info("장바구니 전체 항목 수: {}", basket.getItems().size());

                    int itemCount = 1;

                    // 선택된 장바구니 항목만 처리
                    for (Long itemId : itemIds) {
                        log.info("처리 중인 장바구니 항목 ID: {}", itemId);

                        // 해당 장바구니 항목 찾기
                        BasketItemDTO basketItem = null;
                        for (BasketItemDTO item : basket.getItems()) {
                            if (item.getItemId().equals(itemId)) {
                                basketItem = item;
                                break;
                            }
                        }

                        if (basketItem != null) {
                            log.info("장바구니 항목 찾음: ID={}, 상품코드={}, 수량={}",
                                    basketItem.getItemId(), basketItem.getProductCode(), basketItem.getQuantity());

                            // 상품 정보 조회 (재고 및 배송비 확인)
                            ProductDTO product = productService.getProductDTOByCode(basketItem.getProductCode());
                            if (product != null) {
                                log.info("상품 정보 조회 성공: {}", product.getProductName());

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
                                log.info("주문 항목 추가 완료: 현재 orderItems 크기={}", orderItems.size());
                            } else {
                                log.warn("상품 정보 조회 실패: {}", basketItem.getProductCode());
                            }
                        } else {
                            log.warn("해당 ID의 장바구니 항목을 찾을 수 없음: {}", itemId);
                        }
                    }
                } else {
                    log.warn("장바구니가 null이거나 항목이 없음");
                }
            }

            // 주문 항목이 없는 경우
            if (orderItems.isEmpty()) {
                request.setAttribute("errorMessage", "주문할 상품이 없습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 항목 정보를 세션에 저장 (CreateOrderCommand에서 사용)
            session.setAttribute("orderItems", orderItems);
            session.setAttribute("totalOrderAmount", totalOrderAmount);
            session.setAttribute("totalDeliveryFee", totalDeliveryFee);

            // 주문 양식 페이지로 이동 (주문 정보 전달)
            request.setAttribute("orderItems", orderItems);
            request.setAttribute("totalOrderAmount", totalOrderAmount);
            request.setAttribute("totalDeliveryFee", totalDeliveryFee);

            // 주문 상태 및 결제 상태 코드 리스트 전달
            request.setAttribute("orderStatusList", getOrderStatusList());
            request.setAttribute("paymentStatusList", getPaymentStatusList());

            // 사용자 정보 조회
            UserDTO user = userService.getUserByEmail(userId);
            request.setAttribute("user", user);

            return "/WEB-INF/views/user/orderForm.jsp";

        } catch (Exception e) {
            log.error("주문 양식 표시 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 양식을 표시하는 중 오류가 발생했습니다: " + e.getMessage());
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