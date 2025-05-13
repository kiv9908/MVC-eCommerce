package command.user.product;

import command.Command;
import domain.dto.ProductDTO;
import service.ProductService;
import config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ProductDetailCommand implements Command {
    private ProductService productService;

    public ProductDetailCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();

        // 서비스 초기화 확인
        if (this.productService == null) {
            log.error("ProductService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // URL에서 상품 코드 추출 (/user/product/detail.do?productCode=XXX 형식)
        String productCode = request.getParameter("productCode");

        if (productCode == null || productCode.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "상품 코드가 제공되지 않았습니다.");
            return null;
        }

        // 상품 코드로 상품 정보 조회
        ProductDTO product = productService.getProductDTOByCode(productCode);

        if (product == null) {
            // 상품이 없으면 404 에러
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "상품을 찾을 수 없습니다.");
            return null;
        }

        // 요청 속성 설정
        request.setAttribute("product", product);

        // JSP 페이지 경로 반환
        return "/WEB-INF/views/user/productDetail.jsp";
    }
}