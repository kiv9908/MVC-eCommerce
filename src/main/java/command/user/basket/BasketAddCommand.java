package command.user.basket;

import command.Command;
import domain.dto.BasketItemDTO;
import domain.dto.ProductDTO;
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
public class BasketAddCommand implements Command {

    private final BasketService basketService;
    private final ProductDAO productDAO;

    public BasketAddCommand() {
        BasketDAO basketDAO = new BasketDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.basketService = new BasketService(basketDAO, productDAO);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 파라미터 가져오기
        String productCode = request.getParameter("productCode");
        int quantity = 1; // 기본값

        try {
            if (request.getParameter("quantity") != null) {
                quantity = Integer.parseInt(request.getParameter("quantity"));
            }

            if (quantity <= 0) {
                throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
            }
        } catch (NumberFormatException e) {
            log.error("잘못된 수량 형식: {}", request.getParameter("quantity"));
            quantity = 1;
        }

        // 세션에서 사용자 정보 가져오기
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        boolean result = false;

        // 로그인된 사용자
        if (user != null) {
            log.debug("로그인 사용자 장바구니 추가: userId={}, productCode={}, quantity={}",
                    user.getUserId(), productCode, quantity);

            result = basketService.addToBasket(user.getUserId(), productCode, quantity);
        }
        // 로그인되지 않은 사용자는 세션에 임시 저장
        else {
            log.debug("비로그인 사용자 임시 장바구니 추가: productCode={}, quantity={}", productCode, quantity);

            // 상품 정보 조회
            ProductDTO product = productDAO.findByProductCode(productCode);

            if (product != null) {
                // 세션에서 임시 장바구니 정보 가져오기
                List<BasketItemDTO> tempBasket = (List<BasketItemDTO>) session.getAttribute("tempBasket");

                if (tempBasket == null) {
                    tempBasket = new ArrayList<>();
                }

                // 이미 있는 상품인지 확인
                boolean exists = false;
                for (BasketItemDTO item : tempBasket) {
                    if (item.getProductCode().equals(productCode)) {
                        // 이미 있으면 수량 증가
                        item.setQuantity(item.getQuantity() + quantity);
                        // 금액 재계산
                        item.setAmount(item.getPrice() * item.getQuantity());
                        exists = true;
                        break;
                    }
                }

                // 새 상품이면 추가
                if (!exists) {
                    BasketItemDTO newItem = new BasketItemDTO();
                    newItem.setProductCode(productCode);
                    newItem.setProductName(product.getProductName());
                    newItem.setPrice(product.getSalePrice());
                    newItem.setQuantity(quantity);
                    newItem.setAmount(product.getSalePrice() * quantity);
                    newItem.setFileId(product.getFileId());
                    newItem.setStock(product.getStock());
                    newItem.setDeliveryFee(product.getDeliveryFee());
                    newItem.setStatus(product.getStatus());

                    tempBasket.add(newItem);
                }

                // 세션에 저장
                session.setAttribute("tempBasket", tempBasket);

                // 총 금액 계산
                int totalAmount = 0;
                for (BasketItemDTO item : tempBasket) {
                    totalAmount += item.getAmount();
                }
                session.setAttribute("tempBasketTotalAmount", totalAmount);

                result = true;

                log.debug("임시 장바구니에 상품 추가 완료: productCode={}, 전체 항목 수={}", productCode, tempBasket.size());
            } else {
                log.error("임시 장바구니 추가 실패: 상품을 찾을 수 없음 (productCode={})", productCode);
                result = false;
            }
        }

        // AJAX 요청인 경우 JSON 응답
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            if (result) {
                out.print("{\"success\": true, \"message\": \"상품이 장바구니에 추가되었습니다.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"장바구니에 상품을 추가하는 데 실패했습니다.\"}");
            }
            out.flush();
            return null;
        }

        // 일반 요청인 경우 리다이렉트
        if (result) {
            // 장바구니 페이지로 리다이렉트
            return "redirect:/user/basket.do";
        } else {
            // 에러 메시지 설정
            request.setAttribute("errorMessage", "장바구니에 상품을 추가하는 데 실패했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}