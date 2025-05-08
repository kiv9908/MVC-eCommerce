package command.admin.product;

import command.Command;
import domain.dao.ContentDAO;
import domain.dao.ContentDAOImpl;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;
import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ProductDeleteCommand implements Command {
    private ProductService productService;
    private FileService fileService;
    private boolean useDbStorage = true;

    public ProductDeleteCommand() {
        // DAO와 서비스 초기화
        ProductDAO productDAO = new ProductDAOImpl();
        productService = new ProductService(productDAO);

        // 파일 서비스 초기화 - ServletContext에서 업로드 경로를 가져올 수 없으므로, 생성자에서는 null로 초기화
        ContentDAO contentDAO = new ContentDAOImpl();
        fileService = null; // execute 메소드에서 초기화 예정
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 파일 서비스가 아직 초기화되지 않은 경우 초기화
        if (fileService == null) {
            String uploadPath = request.getServletContext().getRealPath("/uploads");
            ContentDAO contentDAO = new ContentDAOImpl();
            fileService = new FileService(contentDAO, uploadPath, useDbStorage);
        }

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