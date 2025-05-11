package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.MappingService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ProductDeleteCommand implements Command {
    private final ProductService productService;
    private final FileService fileService;
    private final MappingService mappingService;

    public ProductDeleteCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();
        this.fileService = appConfig.getFileService();
        this.mappingService = appConfig.getMappingService();

    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL에서 상품 코드 추출
            String pathInfo = request.getPathInfo();
            String productCode = pathInfo.substring("/delete/".length());

            // 요청 파라미터에서 PageDTO 생성
            PageDTO pageDTO = productService.createPageDTOFromParameters(
                    request.getParameter("page"),
                    request.getParameter("sortBy"),
                    request.getParameter("keyword")
            );

            // 상품 및 관련 데이터 삭제
            boolean success = productService.deleteProductWithRelations(productCode);

            // 리다이렉트 URL 생성
            StringBuilder redirectUrlBuilder = new StringBuilder();
            redirectUrlBuilder.append("redirect:").append(request.getContextPath())
                    .append("/admin/product/list?")
                    .append(success ? "success=delete" : "error=delete");

            // 페이지 정보 추가
            if (pageDTO.getCurrentPage() > 0) {
                redirectUrlBuilder.append("&page=").append(pageDTO.getCurrentPage());
            }

            // 정렬 조건 추가
            if (pageDTO.getSortBy() != null && !pageDTO.getSortBy().isEmpty()) {
                redirectUrlBuilder.append("&sortBy=").append(pageDTO.getSortBy());
            }

            // 검색 키워드 추가 (URL 인코딩 적용)
            if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().isEmpty()) {
                String encodedKeyword = URLEncoder.encode(pageDTO.getKeyword(), StandardCharsets.UTF_8.toString());
                redirectUrlBuilder.append("&keyword=").append(encodedKeyword);
            }

            return redirectUrlBuilder.toString();
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            return "redirect:" + request.getContextPath() + "/admin/product/list?error=delete";
        }
    }
}