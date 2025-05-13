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
public class BasketUpdateCommand implements Command {

    private final BasketService basketService;

    public BasketUpdateCommand() {
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
            return "redirect:/user/login";
        }

        String userId = user.getUserId();
        boolean result = false;

        // 다중 항목 업데이트 (체크박스 선택한 항목들)
        String[] itemIds = request.getParameterValues("itemId");
        String[] quantities = request.getParameterValues("quantity");

        if (itemIds != null && quantities != null && itemIds.length == quantities.length) {
            List<Long> itemIdList = new ArrayList<>();
            List<Integer> quantityList = new ArrayList<>();

            for (int i = 0; i < itemIds.length; i++) {
                try {
                    Long itemId = Long.parseLong(itemIds[i]);
                    int quantity = Integer.parseInt(quantities[i]);

                    if (quantity > 0) {
                        itemIdList.add(itemId);
                        quantityList.add(quantity);
                    }
                } catch (NumberFormatException e) {
                    log.error("잘못된 형식: itemId={}, quantity={}", itemIds[i], quantities[i]);
                }
            }

            if (!itemIdList.isEmpty()) {
                result = basketService.updateSelectedItemsQuantity(userId, itemIdList, quantityList);
            }
        } else {
            // 단일 항목 업데이트
            String itemIdStr = request.getParameter("itemId");
            String quantityStr = request.getParameter("quantity");

            if (itemIdStr != null && quantityStr != null) {
                try {
                    Long itemId = Long.parseLong(itemIdStr);
                    int quantity = Integer.parseInt(quantityStr);

                    if (quantity > 0) {
                        result = basketService.updateBasketItemQuantity(itemId, userId, quantity);
                    }
                } catch (NumberFormatException e) {
                    log.error("잘못된 형식: itemId={}, quantity={}", itemIdStr, quantityStr);
                }
            }
        }

        // result가 false인 경우 알림 표시
        if (!result) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>");
            out.println("alert('재고가 부족합니다. 수량을 다시 확인해주세요.');");
            out.println("location.href='" + request.getContextPath() + "/user/basket.do';");
            out.println("</script>");
            out.close();
            return null; // Command 패턴에서 null을 반환하면 리다이렉트를 수행하지 않음
        }

        // 일반 요청인 경우 리다이렉트
        return "redirect:/user/basket.do";
    }
}