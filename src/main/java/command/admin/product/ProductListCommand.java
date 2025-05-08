package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import service.ProductService;
import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ProductListCommand implements Command {
    private ProductService productService;

    public ProductListCommand() {
        // 서비스 초기화
        ProductDAO productDAO = new ProductDAOImpl();
        this.productService = new ProductService(productDAO);
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("상품 목록 조회 실행");

        try {
            // 정렬 옵션 처리
            String sortBy = request.getParameter("sortBy");
            List<ProductDTO> productDTOs;

            if ("priceAsc".equals(sortBy)) {
                productDTOs = productService.getAllProductDTOsOrderByPrice(true);
            } else if ("priceDesc".equals(sortBy)) {
                productDTOs = productService.getAllProductDTOsOrderByPrice(false);
            } else {
                productDTOs = productService.getAllProductDTOs();
            }

            request.setAttribute("products", productDTOs);

            // 검색 기능
            String searchKeyword = request.getParameter("keyword");
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                List<ProductDTO> searchResults = productService.searchProductDTOs(searchKeyword);
                request.setAttribute("searchResults", searchResults);
                request.setAttribute("searchKeyword", searchKeyword);
            }

            return "/WEB-INF/views/admin/product/productList.jsp";
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}