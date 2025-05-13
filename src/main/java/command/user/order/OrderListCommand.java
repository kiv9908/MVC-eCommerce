package command.user.order;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import command.Command;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.PageDTO;
import domain.dto.UserDTO;
import service.OrderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 주문 목록 조회를 처리하는 Command 클래스
 * URL: /user/order/list.do
 */
@Slf4j
public class OrderListCommand implements Command {

    private OrderService orderService;

    /**
     * 생성자: OrderService 초기화
     */
    public OrderListCommand() {
        this.orderService = new OrderService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("UserOrderListCommand 실행");

        try {
            // 세션에서 로그인한 사용자 정보 확인
            HttpSession session = request.getSession();
            UserDTO loginUser = (UserDTO) session.getAttribute("user");

            if (loginUser == null) {
                // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
                return "redirect:/user/login";
            }

            String userId = loginUser.getUserId();

            // 현재 페이지 파라미터 처리
            int currentPage = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    currentPage = Integer.parseInt(pageParam);
                } catch (NumberFormatException e) {
                    log.warn("잘못된 페이지 번호 형식: " + pageParam);
                }
            }

            // 페이지 크기 파라미터 처리 (기본값: 5)
            int pageSize = 5;
            String pageSizeParam = request.getParameter("pageSize");
            if (pageSizeParam != null && !pageSizeParam.trim().isEmpty()) {
                try {
                    pageSize = Integer.parseInt(pageSizeParam);
                } catch (NumberFormatException e) {
                    log.warn("잘못된 페이지 크기 형식: " + pageSizeParam);
                }
            }

            // PageDTO 생성 및 설정
            PageDTO pageDTO = new PageDTO();
            pageDTO.setCurrentPage(currentPage);
            pageDTO.setPageSize(pageSize);

            // 사용자의 주문 목록 조회
            List<OrderDTO> orderList = orderService.getOrdersByUserId(userId, pageDTO);

            // 각 주문별 대표 상품 정보 조회 (첫 번째 상품만)
            Map<String, OrderItemDTO> representativeItems = new HashMap<>();
            for (OrderDTO order : orderList) {
                List<OrderItemDTO> items = orderService.getOrderItems(order.getOrderId());
                if (items != null && !items.isEmpty()) {
                    representativeItems.put(order.getOrderId(), items.get(0));
                }
            }

            // 주문 목록, 대표 상품 정보 및 페이징 정보를 요청 속성에 저장
            request.setAttribute("orderList", orderList);
            request.setAttribute("representativeItems", representativeItems);
            request.setAttribute("pageDTO", pageDTO);

            // 사용자 주문 목록 페이지 이동
            return "/WEB-INF/views/user/orderList.jsp";

        } catch (Exception e) {
            log.error("사용자 주문 목록 조회 중 오류 발생: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}