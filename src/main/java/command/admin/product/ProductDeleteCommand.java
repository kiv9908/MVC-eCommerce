package command.admin.product;

import command.Command;
import config.AppConfig;
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

            // 현재 페이지, 정렬 조건, 검색 키워드 가져오기
            String page = request.getParameter("page");
            String sortBy = request.getParameter("sortBy");
            String keyword = request.getParameter("keyword");

            // 리다이렉트 URL 구성을 위한 StringBuilder
            StringBuilder redirectUrlBuilder = new StringBuilder();
            redirectUrlBuilder.append("redirect:").append(request.getContextPath()).append("/admin/product/list?success=delete");

            // 페이지 정보 추가
            if (page != null && !page.isEmpty()) {
                redirectUrlBuilder.append("&page=").append(page);
            }

            // 정렬 조건 추가
            if (sortBy != null && !sortBy.isEmpty()) {
                redirectUrlBuilder.append("&sortBy=").append(sortBy);
            }

            // 검색 키워드 추가 (URL 인코딩 적용)
            if (keyword != null && !keyword.isEmpty()) {
                String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
                redirectUrlBuilder.append("&keyword=").append(encodedKeyword);
            }

            ProductDTO productDTO = productService.getProductDTOByCode(productCode);

            // 카테고리 매핑 삭제 로직 (기존 코드 유지)
            try {
                boolean mappingsDeleted = mappingService.deleteAllMappingsByProductCode(productCode);
                if (mappingsDeleted) {
                    log.info("상품 관련 카테고리 매핑 삭제 완료: ProductCode={}", productCode);
                } else {
                    log.warn("상품 관련 카테고리 매핑 삭제 실패: ProductCode={}", productCode);
                }
            } catch (Exception e) {
                log.error("상품 관련 카테고리 매핑 삭제 중 오류 발생: {}", e.getMessage(), e);
            }

            // 상품 삭제
            boolean success = productService.deleteProduct(productCode);

            if (success) {
                // 이미지 파일 삭제 로직 (기존 코드 유지)
                if (productDTO != null && productDTO.getFileId() != null && !productDTO.getFileId().isEmpty()) {
                    boolean fileDeleted = fileService.deleteFile(productDTO.getFileId());
                    if (fileDeleted) {
                        log.info("상품 이미지 삭제 완료: ProductCode={}, FileID={}", productCode, productDTO.getFileId());
                    } else {
                        log.warn("상품은 삭제되었으나 이미지 삭제 실패: ProductCode={}, FileID={}", productCode, productDTO.getFileId());
                    }
                }

                return redirectUrlBuilder.toString();
            } else {
                // 실패 시에도 같은 파라미터 유지
                String errorUrl = redirectUrlBuilder.toString().replace("success=delete", "error=delete");
                return errorUrl;
            }
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            // 기본 에러 리다이렉트 (파라미터 없음)
            return "redirect:" + request.getContextPath() + "/admin/product/list?error=delete";
        }
    }
}