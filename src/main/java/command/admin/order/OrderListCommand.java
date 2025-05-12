package command.admin.order;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import command.Command;
import domain.dto.OrderDTO;
import domain.dto.PageDTO;
import domain.dto.UserDTO;
import service.OrderService;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자용 주문 목록 조회를 처리하는 Command 클래스
 * URL: /admin/order/list
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

        log.info("OrderListCommand 실행");

        try {
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

            // 페이지 크기 파라미터 처리 (기본값: 10)
            int pageSize = 10;
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

            // 주문 목록 조회
            List<OrderDTO> orderList = orderService.getOrderList(pageDTO);

            // 주문 목록 및 페이징 정보를 요청 속성에 저장
            request.setAttribute("orderList", orderList);
            request.setAttribute("pageDTO", pageDTO);

            // 관리자용 주문 목록 페이지 이동
            return "/WEB-INF/views/admin/order/orderList.jsp";

        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}