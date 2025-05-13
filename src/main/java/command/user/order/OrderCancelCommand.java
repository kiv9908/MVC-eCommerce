package command.user.order;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import command.Command;
import domain.dto.OrderDTO;
import domain.dto.UserDTO;
import service.OrderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 주문 취소를 처리하는 Command 클래스
 * URL: /user/order/cancel.do (POST)
 */
@Slf4j
public class OrderCancelCommand implements Command {

    private OrderService orderService;

    /**
     * 생성자: OrderService 초기화
     */
    public OrderCancelCommand() {
        this.orderService = new OrderService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("UserOrderCancelCommand 실행");

        // POST 요청 확인 (주문 취소는 POST로 처리)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        try {
            // 세션에서 로그인한 사용자 정보 확인
            HttpSession session = request.getSession();
            UserDTO loginUser = (UserDTO) session.getAttribute("user");

            if (loginUser == null) {
                // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
                return "redirect:/user/login";
            }

            String userId = loginUser.getUserId();

            // 주문 ID 파라미터 처리
            String orderId = request.getParameter("orderId");

            if (orderId == null || orderId.trim().isEmpty()) {
                request.setAttribute("errorMessage", "주문 ID가 필요합니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 조회
            OrderDTO order = orderService.getOrderDetail(orderId);
            if (order == null) {
                request.setAttribute("errorMessage", "존재하지 않는 주문입니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 본인의 주문인지 확인
            if (!userId.equals(order.getUserId())) {
                request.setAttribute("errorMessage", "다른 사용자의 주문은 취소할 수 없습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 취소 가능 여부 확인 (배송 전 상태일 경우만 취소 가능)
            if (!"ORD1".equals(order.getOrderStatus()) && !"ORD2".equals(order.getOrderStatus())) {
                request.setAttribute("errorMessage", "배송이 시작된 주문은 취소할 수 없습니다. 고객센터로 문의해주세요.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 상태 업데이트 (취소 상태로)
            boolean success = orderService.updateOrderStatus(orderId, "ORD5");

            // 결제 상태도 취소로 업데이트
            if (success) {
                success = orderService.updatePaymentStatus(orderId, "PAY3");
            }

            if (success) {
                // 성공 메시지 설정
                request.setAttribute("message", "주문이 성공적으로 취소되었습니다.");
                // 주문 목록 페이지로 리다이렉트
                return "redirect:/user/order/list.do";
            } else {
                // 실패 메시지 설정
                request.setAttribute("errorMessage", "주문 취소에 실패했습니다. 고객센터로 문의해주세요.");
                return "/WEB-INF/views/common/error.jsp";
            }

        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 취소 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}