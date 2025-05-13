package command.user.basket;

import command.Command;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.BasketService;
import domain.dao.BasketDAO;
import domain.dao.BasketDAOImpl;
import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BasketDeleteCommand implements Command {

    private final BasketService basketService;

    public BasketDeleteCommand() {
        BasketDAO basketDAO = new BasketDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();
        this.basketService = new BasketService(basketDAO, productDAO);
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 세션에서 사용자 정보 가져오기
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/user/login.do";
        }

        String userId = user.getUserId();
        boolean result = false;

        // 다중 항목 삭제 (체크박스 선택한 항목들)
        String[] itemIds = request.getParameterValues("itemId");

        if (itemIds != null && itemIds.length > 0) {
            List<Long> itemIdList = new ArrayList<>();

            for (String idStr : itemIds) {
                try {
                    Long itemId = Long.parseLong(idStr);
                    itemIdList.add(itemId);
                } catch (NumberFormatException e) {
                    log.error("잘못된 형식: itemId={}", idStr);
                }
            }

            if (!itemIdList.isEmpty()) {
                result = basketService.removeSelectedItems(userId, itemIdList);
            }
        } else {
            // 단일 항목 삭제
            String itemIdStr = request.getParameter("itemId");

            if (itemIdStr != null) {
                try {
                    Long itemId = Long.parseLong(itemIdStr);
                    result = basketService.removeBasketItem(itemId, userId);
                } catch (NumberFormatException e) {
                    log.error("잘못된 형식: itemId={}", itemIdStr);
                }
            }
        }

        // AJAX 요청인 경우 JSON 응답
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            if (result) {
                out.print("{\"success\": true, \"message\": \"선택한 상품이 장바구니에서 삭제되었습니다.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"상품 삭제에 실패했습니다.\"}");
            }
            out.flush();
            return null;
        }

        // 일반 요청인 경우 리다이렉트
        return "redirect:/user/basket.do";
    }
}