package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.ProductDTO;

import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

@Slf4j
public class ProductEditCommand implements Command {
    private final ProductService productService;
    private final FileService fileService;

    public ProductEditCommand() {
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

        String method = request.getMethod();

        if ("GET".equals(method)) {
            // GET 요청 처리 - 수정 폼 표시
            return handleGetRequest(request, response);
        } else if ("POST".equals(method)) {
            // POST 요청 처리 - 수정 처리
            return handlePostRequest(request, response);
        }

        // 지원하지 않는 HTTP 메소드
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return null;
    }

    private String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL 형식: /admin/product/edit/{productCode}
            String pathInfo = request.getPathInfo();
            String productCode = pathInfo.substring("/edit/".length());

            // 수정할 상품 정보 조회
            ProductDTO productDTO = productService.getProductDTOByCode(productCode);
            if (productDTO == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "상품을 찾을 수 없습니다.");
                return null;
            }

            request.setAttribute("product", productDTO);

            // 통합된 상품 페이지로 포워딩
            return "/WEB-INF/views/admin/product/productEdit.jsp";
        } catch (Exception e) {
            log.error("상품 수정 폼 표시 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 수정 폼을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }

    private String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // 폼 데이터로부터 DTO 생성
            ProductDTO productDTO = parseProductDTOFromRequest(request);

            // DTO 유효성 검증
            if (!productDTO.isValid()) {
                request.setAttribute("errorMessage", "상품명과 판매가격은 필수 입력 항목입니다.");
                request.setAttribute("product", productDTO);
                return "/WEB-INF/views/admin/product/productEdit.jsp";
            }

            // 이미지 처리
            String userId = productDTO.getRegisterId();
            String fileDeleteOption = request.getParameter("fileDeleteOption");

            if ("delete".equals(fileDeleteOption)) {
                // 기존 이미지 삭제
                if (productDTO.getFileId() != null && !productDTO.getFileId().isEmpty()) {
                    fileService.deleteFile(productDTO.getFileId());
                    productDTO.setFileId(null);
                    log.info("상품 이미지 삭제 완료: ProductCode={}", productDTO.getProductCode());
                }
            } else {
                // 새 이미지 업로드 처리
                String fileId = handleFileUpload(request, userId);
                if (fileId != null) {
                    // 기존 이미지가 있으면 삭제
                    if (productDTO.getFileId() != null && !productDTO.getFileId().isEmpty()) {
                        fileService.deleteFile(productDTO.getFileId());
                    }
                    productDTO.setFileId(fileId);
                    log.info("상품 이미지 업데이트 완료: FileID={}", fileId);
                }
            }

            // 상태 관리 액션 처리
            String statusAction = request.getParameter("statusAction");
            if (statusAction != null && !"none".equals(statusAction)) {
                boolean actionSuccess = false;

                switch (statusAction) {
                    case "start": // 판매 시작
                        actionSuccess = productService.startSelling(productDTO.getProductCode());
                        break;
                    case "stop": // 판매 중지
                        actionSuccess = productService.stopSelling(productDTO.getProductCode());
                        break;
                    case "soldout": // 품절 처리
                        actionSuccess = productService.markAsSoldOut(productDTO.getProductCode());
                        break;
                    default:
                        actionSuccess = true; // 기본값
                }

                if (!actionSuccess) {
                    request.setAttribute("errorMessage", "상품 상태 변경에 실패했습니다.");
                    request.setAttribute("product", productDTO);
                    return "/WEB-INF/views/admin/product/productEdit.jsp";
                }

                // 상태 변경 후 최신 정보로 DTO 갱신
                productDTO = productService.getProductDTOByCode(productDTO.getProductCode());
            }

            // 상품 데이터 업데이트
            boolean success = productService.updateProduct(productDTO);

            if (success) {
                return "redirect:" + request.getContextPath() + "/admin/product/list?success=update";
            } else {
                request.setAttribute("errorMessage", "상품 수정에 실패했습니다.");
                request.setAttribute("product", productDTO);
                return "/WEB-INF/views/admin/product/productEdit.jsp";
            }
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/admin/product/productEdit.jsp";
        }
    }

    // 폼 데이터로부터 ProductDTO 생성
    private ProductDTO parseProductDTOFromRequest(HttpServletRequest request) {
        ProductDTO dto = new ProductDTO();

        // productCode 파라미터가 있는 경우 (수정 시)
        String productCode = request.getParameter("productCode");
        if (productCode != null && !productCode.isEmpty()) {
            dto.setProductCode(productCode);

            // 상품 상태 (수정 시에만 해당)
            String status = request.getParameter("status");
            if (status != null && !status.isEmpty()) {
                dto.setStatus(status);
            }
        }

        // 이름과 설명
        dto.setProductName(request.getParameter("productName"));
        dto.setDetailExplain(request.getParameter("detailExplain"));

        // 기존 파일 ID (수정 시)
        String fileId = request.getParameter("fileId");
        if (fileId != null && !fileId.isEmpty()) {
            dto.setFileId(fileId);
        }

        // 가격 정보
        if (request.getParameter("customerPrice") != null && !request.getParameter("customerPrice").isEmpty()) {
            dto.setCustomerPrice(Integer.parseInt(request.getParameter("customerPrice")));
        }

        if (request.getParameter("salePrice") != null && !request.getParameter("salePrice").isEmpty()) {
            dto.setSalePrice(Integer.parseInt(request.getParameter("salePrice")));
        }

        // 재고
        if (request.getParameter("stock") != null && !request.getParameter("stock").isEmpty()) {
            int stock = Integer.parseInt(request.getParameter("stock"));
            dto.setStock(stock);

            // 품절 자동 처리 - 재고가 0이고 품절 상태로 설정하지 않았을 경우
            if (stock == 0 && (dto.getStatus() == null || !dto.getStatus().equals("품절"))) {
                dto.setStatus("품절");
            }
        }

        // 배송비
        if (request.getParameter("deliveryFee") != null && !request.getParameter("deliveryFee").isEmpty()) {
            dto.setDeliveryFee(Integer.parseInt(request.getParameter("deliveryFee")));
        }

        // 판매 기간
        dto.setStartDate(request.getParameter("startDate"));
        dto.setEndDate(request.getParameter("endDate"));

        // 등록자 ID 설정
        String userId = null;
        if (request.getSession().getAttribute("user") != null) {
            userId = ((UserDTO) request.getSession().getAttribute("user")).getUserId();
        } else {
            userId = "admin"; // 기본값
        }
        dto.setRegisterId(userId);

        return dto;
    }

    /**
     * 파일 업로드 처리
     * @param request HTTP 요청
     * @param userId 사용자 ID
     * @return 업로드된 파일 ID (업로드 실패 시 null)
     */
    private String handleFileUpload(HttpServletRequest request, String userId) {
        try {
            Part filePart = request.getPart("productImage");
            if (filePart != null && filePart.getSize() > 0) {
                return fileService.uploadFile(filePart, "product", userId);
            }
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
        }
        return null;
    }
}