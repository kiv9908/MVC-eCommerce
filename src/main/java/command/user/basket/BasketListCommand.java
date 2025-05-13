package command.user.basket;

import command.Command;
import domain.dto.BasketDTO;
import domain.dto.BasketItemDTO;
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
import java.util.List;

@Slf4j
public class BasketListCommand implements Command {

    private final BasketService basketService;

    public BasketListCommand() {
        BasketDAO basketDAO = new BasketDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();
        this.basketService = new BasketService(basketDAO, productDAO);
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 세션에서 사용자 정보 가져오기
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        // 로그인되지 않은 경우
        if (user == null) {
            log.debug("로그인되지 않은 사용자의 장바구니 접근");
            return "/WEB-INF/views/user/basket.jsp";
        }

        // 로그인된 경우, 임시 장바구니 정보가 있으면 실제 장바구니로 이동
        if (session.getAttribute("tempBasket") != null) {
            moveTempBasketToReal(user.getUserId(), session);
        }

        // 장바구니 조회 (없으면 생성)
        BasketDTO basket = basketService.getOrCreateBasket(user.getUserId());
        request.setAttribute("basket", basket);

        log.debug("로그인 사용자 장바구니 조회: userId={}, itemCount={}",
                user.getUserId(),
                basket != null && basket.getItems() != null ? basket.getItems().size() : 0);

        return "/WEB-INF/views/user/basket.jsp";
    }

    /**
     * 세션의 임시 장바구니 정보를 실제 장바구니로 이동
     */
    @SuppressWarnings("unchecked")
    private void moveTempBasketToReal(String userId, HttpSession session) {
        try {
            // 세션에서 임시 장바구니 정보 가져오기
            List<BasketItemDTO> tempBasket = (List<BasketItemDTO>) session.getAttribute("tempBasket");

            if (tempBasket != null && !tempBasket.isEmpty()) {
                log.debug("임시 장바구니 정보를 실제 장바구니로 이동: userId={}, itemCount={}", userId, tempBasket.size());

                // 모든 임시 항목을 실제 장바구니에 추가
                for (BasketItemDTO tempItem : tempBasket) {
                    basketService.addToBasket(userId, tempItem.getProductCode(), tempItem.getQuantity());
                }

                // 세션에서 임시 장바구니 정보 제거
                session.removeAttribute("tempBasket");
                log.debug("임시 장바구니 정보 이동 완료 및 세션에서 제거");
            }
        } catch (Exception e) {
            log.error("임시 장바구니 정보 이동 중 오류 발생", e);
        }
    }
}