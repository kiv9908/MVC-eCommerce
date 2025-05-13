package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ProductListCommand implements Command {
    private final ProductService productService;
    private final FileService fileService;

    public ProductListCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();
        this.fileService = appConfig.getFileService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("상품 목록 조회 실행");

        try {
            // 요청 파라미터에서 PageDTO 생성
            PageDTO pageDTO = productService.createPageDTOFromParameters(
                    request.getParameter("page"),
                    request.getParameter("sortBy"),
                    request.getParameter("keyword")
            );

            // sortBy가 없는 경우 기본값 설정
            if (pageDTO.getSortBy() == null || pageDTO.getSortBy().isEmpty()) {
                pageDTO.setSortBy("priceAsc");
            }

            // 서비스 계층에서 페이지네이션 설정
            pageDTO = productService.setupProductPage(pageDTO);

            // 상품 목록 조회
            List<ProductDTO> products = productService.getProductsByPage(pageDTO);

            // 결과 저장
            if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().trim().isEmpty()) {
                request.setAttribute("searchResults", products);
                request.setAttribute("searchKeyword", pageDTO.getKeyword());
            } else {
                request.setAttribute("products", products);
            }

            // PageDTO를 request에 저장
            request.setAttribute("pageDTO", pageDTO);

            return "/WEB-INF/views/admin/product/productList.jsp";
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}