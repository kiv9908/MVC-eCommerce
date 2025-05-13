package command.user.order;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import command.Command;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.UserDTO;
import service.OrderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 주문 상세 정보 조회를 처리하는 Command 클래스
 * URL: /user/order/detail.do?orderId=xxx
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

        log.info("UserOrderDetailCommand 실행");

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

            // 주문 상세 정보 조회
            OrderDTO order = orderService.getOrderDetail(orderId);
            if (order == null) {
                request.setAttribute("errorMessage", "존재하지 않는 주문입니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 본인의 주문인지 확인
            if (!userId.equals(order.getUserId())) {
                request.setAttribute("errorMessage", "다른 사용자의 주문은 조회할 수 없습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }

            // 주문 항목 목록 조회
            List<OrderItemDTO> orderItems = orderService.getOrderItems(orderId);

            // 주문 정보 및 주문 항목 목록을 요청 속성에 저장
            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);

            // 주문 취소 가능 여부 확인 (배송 전 상태일 경우만 취소 가능)
            boolean canCancel = "ORD1".equals(order.getOrderStatus()) ||
                    "ORD2".equals(order.getOrderStatus());
            request.setAttribute("canCancel", canCancel);

            // 사용자 주문 상세 페이지 이동
            return "/WEB-INF/views/user/orderDetail.jsp";

        } catch (Exception e) {
            log.error("사용자 주문 상세 조회 중 오류 발생: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 상세 정보를 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}