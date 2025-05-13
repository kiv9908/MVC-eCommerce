package command.admin.order;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import command.Command;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.UserDTO;
import service.OrderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 세부 정보 조회를 처리하는 Command 클래스
 * URL: /admin/order/detail/{orderId}
 */
@Slf4j
public class OrderDetailCommand implements Command {

    private OrderService orderService;

    /**
     * 생성자: OrderService 초기화
     */
    public OrderDetailCommand() {
        this.orderService = new OrderService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("OrderDetailCommand 실행");

        try {
            // 주문 ID 파라미터 처리
            String orderId = extractOrderIdFromURL(request);

            if (orderId == null || orderId.trim().isEmpty()) {
                orderId = request.getParameter("orderId");
            }

            if (orderId == null || orderId.trim().isEmpty()) {
                request.setAttribute("errorMessage", "주문 ID가 필요합니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 상세 정보 조회
            OrderDTO order = orderService.getOrderDetail(orderId);
            if (order == null) {
                request.setAttribute("errorMessage", "존재하지 않는 주문입니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 항목 목록 조회
            List<OrderItemDTO> orderItems = orderService.getOrderItems(orderId);

            // 주문 정보 및 주문 항목 목록을 요청 속성에 저장
            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);

            // 주문 상태 코드 리스트 추가 (상태 변경 드롭다운용)
            request.setAttribute("orderStatusList", getOrderStatusList());
            request.setAttribute("paymentStatusList", getPaymentStatusList());

            // 주문 상세 페이지 이동
            return "/WEB-INF/views/admin/order/orderDetail.jsp";

        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류 발생: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 상세 정보를 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }

    /**
     * URL에서 주문 ID 추출
     * URL 패턴: /admin/order/detail/{orderId}
     */
    private String extractOrderIdFromURL(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String commandPath = requestURI.substring(contextPath.length());

        // /admin/order/detail/{orderId} 형태에서 orderId 추출
        String prefix = "/admin/order/detail/";
        if (commandPath.startsWith(prefix) && commandPath.length() > prefix.length()) {
            return commandPath.substring(prefix.length());
        }

        return null;
    }

    /**
     * 주문 상태 코드 리스트 생성
     * @return 주문 상태 코드 맵
     */
    private List<String[]> getOrderStatusList() {
        List<String[]> statusList = new java.util.ArrayList<>();
        statusList.add(new String[]{"10", "주문완료"});
        statusList.add(new String[]{"30", "배송 전"});
        statusList.add(new String[]{"40", "배송 중"});
        statusList.add(new String[]{"50", "배송 완료"});
        statusList.add(new String[]{"60", "주문 취소"});
        return statusList;
    }

    /**
     * 결제 상태 코드 리스트 생성
     * @return 결제 상태 코드 맵
     */
    private List<String[]> getPaymentStatusList() {
        List<String[]> statusList = new java.util.ArrayList<>();
        statusList.add(new String[]{"20", "결제완료"});
        statusList.add(new String[]{"70", "결제취소"});
        return statusList;
    }
}