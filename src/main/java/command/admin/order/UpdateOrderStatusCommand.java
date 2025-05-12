package command.admin.order;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import command.Command;
import domain.dto.OrderDTO;
import domain.dto.UserDTO;
import service.OrderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 배송 상태 변경을 처리하는 Command 클래스
 * URL: /admin/order/update (POST)
 */
@Slf4j
public class UpdateOrderStatusCommand implements Command {

    private OrderService orderService;

    /**
     * 생성자: OrderService 초기화
     */
    public UpdateOrderStatusCommand() {
        this.orderService = new OrderService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("UpdateOrderStatusCommand 실행");

        // POST 요청 확인 (상태 변경은 POST로 처리)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        try {
            // 파라미터 처리
            String orderId = request.getParameter("orderId");
            String newStatus = request.getParameter("newStatus");

            if (orderId == null || orderId.trim().isEmpty() ||
                    newStatus == null || newStatus.trim().isEmpty()) {
                request.setAttribute("errorMessage", "주문 ID와 새로운 상태 정보가 필요합니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 조회
            OrderDTO order = orderService.getOrderDetail(orderId);
            if (order == null) {
                request.setAttribute("errorMessage", "존재하지 않는 주문입니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 상태 변경 가능 여부 확인 (예: 취소된 주문은 다른 상태로 변경 불가)
            if ("ORD5".equals(order.getOrderStatus()) || "ORD6".equals(order.getOrderStatus())) {
                if (!newStatus.equals(order.getOrderStatus())) {
                    request.setAttribute("errorMessage", "취소 또는 환불된 주문의 상태는 변경할 수 없습니다.");
                    return "/WEB-INF/views/common/error.jsp";
                }
            }

            // 주문 상태 업데이트
            boolean success = orderService.updateOrderStatus(orderId, newStatus);

            if (success) {
                // 상태 변경 이력 저장 (옵션 - 별도 테이블 필요)
                // orderService.addOrderStatusHistory(orderId, order.getOrderStatus(), newStatus, loginUser.getUserId());

                // 성공 메시지 설정
                request.setAttribute("message", "주문 상태가 성공적으로 업데이트되었습니다.");

                // 주문 상세 페이지로 리다이렉트
                return "redirect:/admin/order/detail/" + orderId;
            } else {
                // 실패 메시지 설정
                request.setAttribute("errorMessage", "주문 상태 업데이트에 실패했습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

        } catch (Exception e) {
            log.error("주문 상태 업데이트 중 오류 발생: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 상태를 업데이트하는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}