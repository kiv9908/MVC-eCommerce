package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ProductDeleteCommand implements Command {
    private final ProductService productService;
    private final FileService fileService;

    public ProductDeleteCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();
        this.fileService = appConfig.getFileService();

        // fileService가 null이면 로그 남기기
        if (this.fileService == null) {
            log.error("FileService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // fileService 초기화 코드 제거 (생성자에서 이미 초기화됨)

        try {
            // URL에서 상품 코드 추출
            String pathInfo = request.getPathInfo();
            String productCode = pathInfo.substring("/delete/".length());

            // 상품 정보 조회 (이미지 ID 확인을 위해)
            ProductDTO productDTO = productService.getProductDTOByCode(productCode);

            // 상품 삭제
            boolean success = productService.deleteProduct(productCode);

            if (success) {
                // 연결된 이미지 파일 삭제
                if (productDTO != null && productDTO.getFileId() != null && !productDTO.getFileId().isEmpty()) {
                    boolean fileDeleted = fileService.deleteFile(productDTO.getFileId());
                    if (fileDeleted) {
                        log.info("상품 이미지 삭제 완료: ProductCode={}, FileID={}", productCode, productDTO.getFileId());
                    } else {
                        log.warn("상품은 삭제되었으나 이미지 삭제 실패: ProductCode={}, FileID={}", productCode, productDTO.getFileId());
                    }
                }

                return "redirect:" + request.getContextPath() + "/admin/product/list?success=delete";
            } else {
                return "redirect:" + request.getContextPath() + "/admin/product/list?error=delete";
            }
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            return "redirect:" + request.getContextPath() + "/admin/product/list?error=delete";
        }
    }
}