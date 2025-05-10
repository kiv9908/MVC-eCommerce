package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;
import domain.dto.ProductDTO;
import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.MappingService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProductCreateCommand implements Command {
    private final ProductService productService;
    private final FileService fileService;
    private final MappingService mappingService; // 추가된 의존성

    public ProductCreateCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();
        this.fileService = appConfig.getFileService();
        this.mappingService = appConfig.getMappingService(); // 추가된 의존성 초기화

        // 서비스 초기화 확인
        if (this.fileService == null) {
            log.error("FileService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }

        if (this.mappingService == null) {
            log.error("MappingService가 초기화되지 않았습니다. AppConfig 설정을 확인해주세요.");
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();

        if ("GET".equals(method)) {
            // GET 요청 처리 - 생성 폼 표시
            return handleGetRequest(request, response);
        } else if ("POST".equals(method)) {
            // POST 요청 처리 - 생성 처리
            return handlePostRequest(request, response);
        }

        // 지원하지 않는 HTTP 메소드
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return null;
    }

    private String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 빈 DTO 생성하여 폼에 전달 (생성 모드)
            ProductDTO emptyDTO = new ProductDTO();
            request.setAttribute("product", emptyDTO);

            // 사용 가능한 카테고리 목록 로드
            try {
                List<CategoryDTO> availableCategories = mappingService.getAllCategories();
                request.setAttribute("availableCategories", availableCategories);
                log.info("사용 가능한 카테고리 수: {}", availableCategories.size());
            } catch (Exception e) {
                log.error("카테고리 정보 로딩 중 오류 발생: {}", e.getMessage(), e);
                // 오류가 발생해도 페이지는 계속 로드
            }

            // 통합된 상품 페이지로 포워딩
            return "/WEB-INF/views/admin/product/productEdit.jsp";
        } catch (Exception e) {
            log.error("상품 생성 폼 표시 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 생성 폼을 불러오는 중 오류가 발생했습니다.");
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

            // 파일 업로드 처리
            String userId = productDTO.getRegisterId();
            String fileId = handleFileUpload(request, userId);
            if (fileId != null) {
                productDTO.setFileId(fileId);
                log.info("상품 이미지 업로드 완료: FileID={}", fileId);
            }

            // 상품 저장
            boolean success = productService.createProduct(productDTO);

            if (success) {
                // 상품 등록 성공 후 카테고리 매핑 처리
                String productCode = productDTO.getProductCode();

                if (productCode != null && !productCode.isEmpty()) {
                    boolean mappingSuccess = handleCategoryMappings(request, productCode);
                    if (!mappingSuccess) {
                        log.warn("상품은 생성되었으나 카테고리 매핑 처리 중 오류가 발생했습니다.");
                    }
                } else {
                    log.warn("생성된 상품의 코드를 찾을 수 없어 카테고리 매핑을 처리할 수 없습니다.");
                }

                return "redirect:" + request.getContextPath() + "/admin/product/list?success=create";
            } else {
                request.setAttribute("errorMessage", "상품 생성에 실패했습니다.");
                request.setAttribute("product", productDTO);
                return "/WEB-INF/views/admin/product/productEdit.jsp";
            }
        } catch (Exception e) {
            log.error("상품 생성 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/admin/product/productEdit.jsp";
        }
    }

    // 폼 데이터로부터 ProductDTO 생성 (기존 코드 유지)
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

        // 첫 등록일
        if (request.getParameter("productCode") == null) { // 신규 등록인 경우
            dto.setFirstDate(new Date());
            // 신규 등록 시 기본 상태 설정
            dto.setStatus("판매준비중");
        }

        return dto;
    }

    /**
     * 파일 업로드 처리 (기존 코드 유지)
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

    /**
     * 카테고리 매핑 정보 처리
     * @param request HTTP 요청
     * @param productCode 상품 코드
     * @return 처리 성공 여부
     */
    private boolean handleCategoryMappings(HttpServletRequest request, String productCode) {
        try {
            // 새로 추가된 카테고리 매핑 처리
            String[] newCategoryIds = request.getParameterValues("newCategoryIds");
            if (newCategoryIds != null) {
                for (String categoryIdStr : newCategoryIds) {
                    try {
                        Long categoryId = Long.parseLong(categoryIdStr);
                        MappingDTO mappingDTO = new MappingDTO();
                        mappingDTO.setProductCode(productCode);
                        mappingDTO.setCategoryId(categoryId);

                        // 등록자 정보 설정
                        String userId = null;
                        if (request.getSession().getAttribute("user") != null) {
                            userId = ((UserDTO) request.getSession().getAttribute("user")).getUserId();
                        } else {
                            userId = "admin"; // 기본값
                        }
                        mappingDTO.setRegisterUser(userId);

                        // 중복 매핑 확인
                        MappingDTO existingMapping = mappingService.getMappingByProductAndCategory(productCode, categoryId);
                        if (existingMapping == null) {
                            // 새 매핑 생성
                            boolean created = mappingService.createMapping(mappingDTO);
                            if (created) {
                                log.info("새 카테고리 매핑 추가: 상품={}, 카테고리={}", productCode, categoryId);
                            } else {
                                log.warn("카테고리 매핑 추가 실패: 상품={}, 카테고리={}", productCode, categoryId);
                            }
                        } else {
                            log.info("카테고리 매핑이 이미 존재합니다: 상품={}, 카테고리={}", productCode, categoryId);
                        }
                    } catch (NumberFormatException e) {
                        log.error("잘못된 카테고리 ID 형식: {}", categoryIdStr);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("카테고리 매핑 처리 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
}