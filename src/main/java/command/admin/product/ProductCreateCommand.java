package command.admin.product;

import command.Command;
import domain.dao.ContentDAO;
import domain.dao.ContentDAOImpl;
import domain.dto.ProductDTO;
import domain.model.Content;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;
import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class ProductCreateCommand implements Command {
    private ProductService productService;
    private FileService fileService;
    private boolean useDbStorage = true;  // BLOB 저장 방식 활성화

    public ProductCreateCommand() {
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
            userId = ((User) request.getSession().getAttribute("user")).getUserId();
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