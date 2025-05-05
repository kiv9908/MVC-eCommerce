package controller.admin;

import domain.dao.CategoryDAO;
import domain.dao.CategoryDAOImpl;
import domain.model.Category;
import lombok.extern.slf4j.Slf4j;
import service.CategoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
@Slf4j
@WebServlet("/admin/category/*")
public class CategoryManageServlet extends HttpServlet {

    private CategoryService categoryService;
    
    @Override
    public void init() throws ServletException {
        // DAO와 서비스 초기화
        CategoryDAO categoryDAO = new CategoryDAOImpl();
        categoryService = new CategoryService(categoryDAO);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || "/".equals(pathInfo) || "/list".equals(pathInfo)) {
            // 카테고리 목록 페이지
            listCategories(request, response);
        } else if ("/create".equals(pathInfo)) {
            // 카테고리 생성 페이지
            showCreateForm(request, response);
        } else if (pathInfo.startsWith("/edit/")) {
            // 카테고리 수정 페이지
            showEditForm(request, response);
        } else if (pathInfo.startsWith("/delete/")) {
            // 카테고리 삭제 처리
            deleteCategory(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if ("/create".equals(pathInfo)) {
            // 카테고리 생성 처리
            createCategory(request, response);
        } else if ("/edit".equals(pathInfo)) {
            // 카테고리 수정 처리
            updateCategory(request, response);
        } else if ("/toggle-status".equals(pathInfo)) {
            // 카테고리 활성화/비활성화 상태 변경
            toggleCategoryStatus(request, response);
        } else if ("/update-order".equals(pathInfo)) {
            // 카테고리 순서 변경
            updateCategoryOrder(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    // 카테고리 목록 표시
    private void listCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 계층형 구조로 카테고리 조회
            List<Category> categories = categoryService.getAllCategories();
            request.setAttribute("categories", categories);
            
            // (선택) 검색 기능이 있는 경우
            String searchKeyword = request.getParameter("keyword");
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                List<Category> searchResults = categoryService.searchCategories(searchKeyword);
                request.setAttribute("searchResults", searchResults);
                request.setAttribute("searchKeyword", searchKeyword);
            }
            
            // 카테고리 목록 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/categoryManage.jsp").forward(request, response);
        } catch (Exception e) {
            log.info("카테고리 목록 조회 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 목록을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 카테고리 생성 폼 표시
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 상위 카테고리 선택을 위한 카테고리 목록 조회
            List<Category> parentCategories = categoryService.getAllCategories();
            request.setAttribute("parentCategories", parentCategories);
            
            // 카테고리 생성 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/category/create.jsp").forward(request, response);
        } catch (Exception e) {
            log.info("카테고리 생성 폼 표시 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 생성 폼을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 카테고리 수정 폼 표시
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL에서 카테고리 ID 추출
            String pathInfo = request.getPathInfo();
            String categoryIdStr = pathInfo.substring("/edit/".length());
            int categoryId = Integer.parseInt(categoryIdStr);
            
            // 수정할 카테고리 정보 조회
            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "카테고리를 찾을 수 없습니다.");
                return;
            }
            
            // 상위 카테고리 선택을 위한 카테고리 목록 조회 (자기 자신은 제외)
            List<Category> parentCategories = categoryService.getAllCategories();
            parentCategories.removeIf(c -> c.getNbCategory() == categoryId);
            
            request.setAttribute("category", category);
            request.setAttribute("parentCategories", parentCategories);
            
            // 카테고리 수정 페이지로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/admin/category/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
        } catch (Exception e) {
            log.info("카테고리 수정 폼 표시 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 수정 폼을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
        }
    }
    
    // 카테고리 생성 처리
    private void createCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        try {
            // 폼 데이터 가져오기
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String parentIdStr = request.getParameter("parentId");
            String orderStr = request.getParameter("order");
            String useYn = request.getParameter("useYn");
            
            // 세션에서 사용자 ID 가져오기
            String userId = null;
            if (request.getSession().getAttribute("user") != null) {
                userId = ((domain.model.User) request.getSession().getAttribute("user")).getUserId();
            } else {
                userId = "admin"; // 기본값
            }
            
            // 카테고리 객체 생성
            Category category = new Category();
            category.setNmCategory(name);
            category.setNmExplain(description);
            
            if (parentIdStr != null && !parentIdStr.isEmpty()) {
                category.setNbParentCategory(Integer.parseInt(parentIdStr));
            }
            
            if (orderStr != null && !orderStr.isEmpty()) {
                category.setCnOrder(Integer.parseInt(orderStr));
            }
            
            category.setYnUse(useYn != null ? useYn : "Y");
            category.setNoRegister(userId);
            
            // 카테고리 저장
            boolean success = categoryService.createCategory(category);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/category/list?success=create");
            } else {
                request.setAttribute("errorMessage", "카테고리 생성에 실패했습니다.");
                request.setAttribute("category", category);
                request.getRequestDispatcher("/WEB-INF/views/admin/category/create.jsp").forward(request, response);
            }
        } catch (Exception e) {
            log.info("카테고리 생성 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 생성 중 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/category/create.jsp").forward(request, response);
        }
    }
    
    // 카테고리 수정 처리
    private void updateCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        try {
            // 폼 데이터 가져오기
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String parentIdStr = request.getParameter("parentId");
            String orderStr = request.getParameter("order");
            String useYn = request.getParameter("useYn");
            
            // 기존 카테고리 정보 조회
            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "카테고리를 찾을 수 없습니다.");
                return;
            }
            
            // 카테고리 정보 업데이트
            category.setNmCategory(name);
            category.setNmExplain(description);
            
            if (parentIdStr != null && !parentIdStr.isEmpty()) {
                category.setNbParentCategory(Integer.parseInt(parentIdStr));
            } else {
                category.setNbParentCategory(null);
            }
            
            if (orderStr != null && !orderStr.isEmpty()) {
                category.setCnOrder(Integer.parseInt(orderStr));
            }
            
            category.setYnUse(useYn != null ? useYn : "N");
            
            // 카테고리 업데이트
            boolean success = categoryService.updateCategory(category);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/category/list?success=update");
            } else {
                request.setAttribute("errorMessage", "카테고리 수정에 실패했습니다.");
                request.setAttribute("category", category);
                request.getRequestDispatcher("/WEB-INF/views/admin/category/edit.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
        } catch (Exception e) {
            log.info("카테고리 수정 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 수정 중 오류가 발생했습니다: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/category/edit.jsp").forward(request, response);
        }
    }
    
    // 카테고리 삭제 처리
    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL에서 카테고리 ID 추출
            String pathInfo = request.getPathInfo();
            String categoryIdStr = pathInfo.substring("/delete/".length());
            int categoryId = Integer.parseInt(categoryIdStr);
            
            // 카테고리 삭제
            boolean success = categoryService.deleteCategory(categoryId);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/category/list?success=delete");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/category/list?error=delete");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
        } catch (Exception e) {
            log.info("카테고리 삭제 중 오류 발생: {}", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/category/list?error=delete");
        }
    }
    
    // 카테고리 활성화/비활성화 상태 변경 처리
    private void toggleCategoryStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            boolean isActive = "Y".equals(request.getParameter("active"));
            
            boolean success = categoryService.updateCategoryUseStatus(categoryId, isActive);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            if (success) {
                response.getWriter().write("{\"success\": true}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"상태 변경에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 카테고리 ID 형식입니다.\"}");
        } catch (Exception e) {
            log.info("카테고리 상태 변경 중 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
    
    // 카테고리 순서 변경 처리
    private void updateCategoryOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            int newOrder = Integer.parseInt(request.getParameter("order"));
            
            boolean success = categoryService.updateCategoryOrder(categoryId, newOrder);
            
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
            log.info("카테고리 순서 변경 중 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
}