package controller.admin;

import domain.dao.ContentDAO;
import domain.dao.ContentDAOImpl;
import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;
import domain.dto.ProductDTO;
import domain.model.Content;
import domain.model.Product;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;

@Slf4j
@WebServlet("/admin/product/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 10,  // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class ProductManageServlet extends HttpServlet {

    private ProductService productService;
    private FileService fileService;
    private boolean useDbStorage = true;  // BLOB 저장 방식 활성화
    
    @Override
    public void init() throws ServletException {
        // DAO와 서비스 초기화
        ProductDAO productDAO = new ProductDAOImpl();
        productService = new ProductService(productDAO);
        
        // 파일 서비스 초기화
        String uploadPath = getServletContext().getRealPath("/uploads");
        ContentDAO contentDAO = new ContentDAOImpl();
        fileService = new FileService(contentDAO, uploadPath, useDbStorage);
        log.info("ProductManageServlet 초기화 완료. 업로드 경로: {}", uploadPath);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || "/".equals(pathInfo) || "/list".equals(pathInfo)) {
            // 상품 목록 페이지
            listProducts(request, response);
        } else if ("/create".equals(pathInfo)) {
            // 상품 생성 페이지
            showProductForm(request, response, true);
        } else if (pathInfo.startsWith("/edit/")) {
            // 상품 수정 페이지
            showProductForm(request, response, false);
        } else if (pathInfo.startsWith("/delete/")) {
            // 상품 삭제 처리
            deleteProduct(request, response);
        } else if ("/file-info".equals(pathInfo)) {
            // 파일 정보 조회 API
            getFileInfo(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if ("/create".equals(pathInfo)) {
            // 상품 생성 처리
            createProduct(request, response);
        } else if ("/edit".equals(pathInfo)) {
            // 상품 수정 처리 
            updateProduct(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    // 상품 목록 표시
    private void listProducts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            
            // 상품 목록 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/product/productList.jsp").forward(request, response);
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 목록을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 폼 표시 (생성 및 수정)
    private void showProductForm(HttpServletRequest request, HttpServletResponse response, boolean isCreate) throws ServletException, IOException {
        try {
            if (isCreate) {
                // 빈 DTO 생성하여 폼에 전달 (생성 모드)
                ProductDTO emptyDTO = new ProductDTO();
                request.setAttribute("product", emptyDTO);
            } else {
                // URL에서 상품 코드 추출 (수정 모드)
                String pathInfo = request.getPathInfo();
                String productCode = pathInfo.substring("/edit/".length());
                
                // 수정할 상품 정보 조회
                ProductDTO productDTO = productService.getProductDTOByCode(productCode);
                if (productDTO == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "상품을 찾을 수 없습니다.");
                    return;
                }
                
                request.setAttribute("product", productDTO);
            }
            
            // 통합된 상품 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
        } catch (Exception e) {
            String operation = isCreate ? "생성" : "수정";
            log.error("상품 {} 폼 표시 중 오류 발생: {}", operation, e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 " + operation + " 폼을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
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
    
    // 상품 생성 처리
    private void createProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        try {
            // 폼 데이터로부터 DTO 생성
            ProductDTO productDTO = parseProductDTOFromRequest(request);
            
            // DTO 유효성 검증
            if (!productDTO.isValid()) {
                request.setAttribute("errorMessage", "상품명과 판매가격은 필수 입력 항목입니다.");
                request.setAttribute("product", productDTO);
                request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
                return;
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
                response.sendRedirect(request.getContextPath() + "/admin/product/list?success=create");
            } else {
                request.setAttribute("errorMessage", "상품 생성에 실패했습니다.");
                request.setAttribute("product", productDTO);
                request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
            }
        } catch (Exception e) {
            log.error("상품 생성 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 생성 중 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
        }
    }
    
    // 상품 수정 처리
    private void updateProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        try {
            // 폼 데이터로부터 DTO 생성
            ProductDTO productDTO = parseProductDTOFromRequest(request);
            
            // DTO 유효성 검증
            if (!productDTO.isValid()) {
                request.setAttribute("errorMessage", "상품명과 판매가격은 필수 입력 항목입니다.");
                request.setAttribute("product", productDTO);
                request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
                return;
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
                    request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
                    return;
                }
                
                // 상태 변경 후 최신 정보로 DTO 갱신
                productDTO = productService.getProductDTOByCode(productDTO.getProductCode());
            }
            
            // 상품 데이터 업데이트
            boolean success = productService.updateProduct(productDTO);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/product/list?success=update");
            } else {
                request.setAttribute("errorMessage", "상품 수정에 실패했습니다.");
                request.setAttribute("product", productDTO);
                request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
            }
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/product/productEdit.jsp").forward(request, response);
        }
    }
    
    // 상품 삭제 처리
    private void deleteProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                
                response.sendRedirect(request.getContextPath() + "/admin/product/list?success=delete");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/product/list?error=delete");
            }
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/admin/product/list?error=delete");
        }
    }

    /**
     * 파일 정보 조회 API (AJAX 요청 처리)
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    private void getFileInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileId = request.getParameter("fileId");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                // 파일 ID가 없는 경우
                out.print("{\"error\": \"파일 ID가 없습니다.\"}");
                return;
            }
            
            // 파일 정보 조회
            Content content = fileService.getFileById(fileId);
            
            if (content == null) {
                // 파일을 찾을 수 없는 경우
                out.print("{\"error\": \"파일을 찾을 수 없습니다.\"}");
                return;
            }
            
            // JSON 응답 생성
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("fileId", content.getFileId());
            jsonResponse.put("originalFileName", content.getOriginalFileName());
            jsonResponse.put("fileExtension", content.getFileExtension());
            jsonResponse.put("fileType", content.getFileType());
            
            // JSON 응답 전송
            out.print(jsonResponse.toJSONString());
            
        } catch (Exception e) {
            log.error("파일 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            out.print("{\"error\": \"파일 정보 조회 중 오류가 발생했습니다.\"}");
        } finally {
            out.flush();
        }
    }
}
