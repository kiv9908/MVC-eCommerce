package controller.admin;

import domain.dao.*;
import domain.model.Category;
import domain.model.Product;
import service.CategoryProductMappingService;
import service.CategoryService;
import service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/admin/category-product/*")
public class CategoryProductMappingServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(CategoryProductMappingServlet.class.getName());
    
    private CategoryService categoryService;
    private ProductService productService;
    private CategoryProductMappingService mappingService;
    
    @Override
    public void init() throws ServletException {
        // DAO와 서비스 초기화
        CategoryDAO categoryDAO = new CategoryDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();
        CategoryProductMappingDAO mappingDAO = new CategoryProductMappingDAOImpl();
        
        categoryService = new CategoryService(categoryDAO);
        productService = new ProductService(productDAO);
        mappingService = new CategoryProductMappingService(mappingDAO);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || "/".equals(pathInfo)) {
            // 카테고리-상품 관리 메인 페이지
            listCategoryProducts(request, response);
        } else if (pathInfo.startsWith("/category/")) {
            // 특정 카테고리의 상품 목록 페이지
            showCategoryProducts(request, response);
        } else if ("/add".equals(pathInfo)) {
            // 상품 추가 페이지
            showAddProductForm(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if ("/add".equals(pathInfo)) {
            // 카테고리에 상품 추가 처리
            addProductsToCategory(request, response);
        } else if ("/remove".equals(pathInfo)) {
            // 카테고리에서 상품 제거 처리
            removeProductFromCategory(request, response);
        } else if ("/update-order".equals(pathInfo)) {
            // 카테고리 내 상품 순서 변경 처리
            updateProductOrder(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    // 카테고리-상품 관리 메인 페이지
    private void listCategoryProducts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 모든 카테고리 조회
            List<Category> categories = categoryService.getAllCategories();
            request.setAttribute("categories", categories);
            
            // 카테고리-상품 관리 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/category-product/list.jsp").forward(request, response);
        } catch (Exception e) {
            logger.severe("카테고리-상품 목록 조회 중 오류 발생: " + e.getMessage());
            request.setAttribute("errorMessage", "카테고리-상품 목록을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 특정 카테고리의 상품 목록 페이지
    private void showCategoryProducts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL에서 카테고리 ID 추출
            String pathInfo = request.getPathInfo();
            String categoryIdStr = pathInfo.substring("/category/".length());
            int categoryId = Integer.parseInt(categoryIdStr);
            
            // 카테고리 정보 조회
            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "카테고리를 찾을 수 없습니다.");
                return;
            }
            
            // 카테고리에 속한 상품 목록 조회
            List<Product> products = mappingService.getProductsByCategoryId(categoryId);
            
            request.setAttribute("category", category);
            request.setAttribute("products", products);
            
            // 카테고리 상품 목록 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/category-product/category-products.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
        } catch (Exception e) {
            logger.severe("카테고리 상품 목록 조회 중 오류 발생: " + e.getMessage());
            request.setAttribute("errorMessage", "카테고리 상품 목록을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 카테고리에 상품 추가 페이지
    private void showAddProductForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 카테고리 ID 가져오기
            String categoryIdStr = request.getParameter("categoryId");
            if (categoryIdStr == null || categoryIdStr.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "카테고리 ID가 필요합니다.");
                return;
            }
            
            int categoryId = Integer.parseInt(categoryIdStr);
            
            // 카테고리 정보 조회
            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "카테고리를 찾을 수 없습니다.");
                return;
            }
            
            // 현재 카테고리에 속한 상품 목록 조회
            List<Product> categoryProducts = mappingService.getProductsByCategoryId(categoryId);
            
            // 모든 상품 목록 조회
            List<Product> allProducts = productService.findAll();
            
            // 카테고리에 아직 추가되지 않은 상품 필터링
            List<Product> availableProducts = new ArrayList<>();
            for (Product product : allProducts) {
                boolean isAlreadyInCategory = false;
                for (Product categoryProduct : categoryProducts) {
                    if (product.getProductCode().equals(categoryProduct.getProductCode())) {
                        isAlreadyInCategory = true;
                        break;
                    }
                }
                
                if (!isAlreadyInCategory) {
                    availableProducts.add(product);
                }
            }
            
            request.setAttribute("category", category);
            request.setAttribute("availableProducts", availableProducts);
            
            // 상품 추가 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/category-product/add-products.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
        } catch (Exception e) {
            logger.severe("상품 추가 폼 표시 중 오류 발생: " + e.getMessage());
            request.setAttribute("errorMessage", "상품 추가 폼을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 카테고리에 상품 추가 처리
    private void addProductsToCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        try {
            // 폼 데이터 가져오기
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String[] productCodes = request.getParameterValues("productCodes");
            
            if (productCodes == null || productCodes.length == 0) {
                request.setAttribute("errorMessage", "추가할 상품을 선택해주세요.");
                showAddProductForm(request, response);
                return;
            }
            
            // 세션에서 사용자 ID 가져오기
            String userId = null;
            if (request.getSession().getAttribute("user") != null) {
                userId = ((domain.model.User) request.getSession().getAttribute("user")).getUserId();
            } else {
                userId = "admin"; // 기본값
            }
            
            // 카테고리에 상품 추가
            boolean success = mappingService.addProductsToCategory(categoryId, Arrays.asList(productCodes), userId);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/category-product/category/" + categoryId + "?success=add");
            } else {
                request.setAttribute("errorMessage", "상품 추가에 실패했습니다.");
                showAddProductForm(request, response);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
        } catch (Exception e) {
            logger.severe("상품 추가 중 오류 발생: " + e.getMessage());
            request.setAttribute("errorMessage", "상품 추가 중 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 카테고리에서 상품 제거 처리
    private void removeProductFromCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String productCode = request.getParameter("productCode");
            
            boolean success = mappingService.removeProductFromCategory(categoryId, productCode);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            if (success) {
                response.getWriter().write("{\"success\": true}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"상품 제거에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 카테고리 ID 형식입니다.\"}");
        } catch (Exception e) {
            logger.severe("상품 제거 중 오류 발생: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // 카테고리 내 상품 순서 변경 처리
    private void updateProductOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String productCode = request.getParameter("productCode");
            int newOrder = Integer.parseInt(request.getParameter("order"));
            
            boolean success = mappingService.updateProductOrderInCategory(categoryId, productCode, newOrder);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            if (success) {
                response.getWriter().write("{\"success\": true}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"순서 변경에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 숫자 형식입니다.\"}");
        } catch (Exception e) {
            logger.severe("상품 순서 변경 중 오류 발생: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
}