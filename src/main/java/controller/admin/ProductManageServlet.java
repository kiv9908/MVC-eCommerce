package controller.admin;

import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;
import domain.dto.ProductDTO;
import domain.model.Product;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@WebServlet("/admin/product/*")
public class ProductManageServlet extends HttpServlet {

    private ProductService productService;
    
    @Override
    public void init() throws ServletException {
        // DAO와 서비스 초기화
        ProductDAO productDAO = new ProductDAOImpl();
        productService = new ProductService(productDAO);
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
        dto.setFileId(request.getParameter("fileId"));
        
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
            
            // 상품 삭제
            boolean success = productService.deleteProduct(productCode);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/product/list?success=delete");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/product/list?error=delete");
            }
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            response.sendRedirect(request.getContextPath() + "/admin/product/list?error=delete");
        }
    }

}
