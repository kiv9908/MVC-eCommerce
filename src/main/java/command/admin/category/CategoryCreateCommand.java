package command.admin.category;

import command.Command;
import config.AppConfig;
import domain.dto.CategoryDTO;
import domain.model.User;
import lombok.extern.slf4j.Slf4j;
import service.CategoryService;
import domain.dao.CategoryDAO;
import domain.dao.CategoryDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class CategoryCreateCommand implements Command {
    private final CategoryService categoryService;

    public CategoryCreateCommand() {
        // 서비스 초기화
        AppConfig appConfig = AppConfig.getInstance();
        this.categoryService = appConfig.getCategoryService();
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
            // 새 카테고리 생성인 경우, 빈 DTO 생성
            CategoryDTO categoryDTO = new CategoryDTO();
            // 상위 카테고리 목록 가져오기
            List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs();

            request.setAttribute("category", categoryDTO);
            request.setAttribute("parentCategories", parentCategoryDTOs);

            return "/WEB-INF/views/admin/category/categoryEdit.jsp";
        } catch (Exception e) {
            log.info("카테고리 생성 폼 표시 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 폼을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }

    private String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // 폼 데이터로부터 DTO 생성
            CategoryDTO categoryDTO = parseCategoryDTOFromRequest(request);

            // DTO 유효성 검증
            if (!categoryDTO.isValid()) {
                request.setAttribute("errorMessage", "카테고리명은 필수 입력 항목입니다.");
                request.setAttribute("category", categoryDTO);

                // 상위 카테고리 목록 가져오기 (폼 재표시용)
                List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs();
                request.setAttribute("parentCategories", parentCategoryDTOs);

                return "/WEB-INF/views/admin/category/categoryEdit.jsp";
            }

            // 카테고리 저장
            boolean success = categoryService.createCategory(categoryDTO);

            if (success) {
                return "redirect:" + request.getContextPath() + "/admin/category/list?success=create";
            } else {
                request.setAttribute("errorMessage", "카테고리 생성에 실패했습니다.");
                request.setAttribute("category", categoryDTO);

                // 상위 카테고리 목록 가져오기 (폼 재표시용)
                List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs();
                request.setAttribute("parentCategories", parentCategoryDTOs);

                return "/WEB-INF/views/admin/category/categoryEdit.jsp";
            }
        } catch (Exception e) {
            log.info("카테고리 생성 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 생성 중 오류가 발생했습니다: " + e.getMessage());

            // 상위 카테고리 목록 가져오기 (폼 재표시용)
            try {
                CategoryDTO categoryDTO = parseCategoryDTOFromRequest(request);
                request.setAttribute("category", categoryDTO);

                List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs();
                request.setAttribute("parentCategories", parentCategoryDTOs);
            } catch (Exception ex) {
                log.error("상위 카테고리 목록 조회 중 오류 발생: {}", ex.getMessage());
            }

            return "/WEB-INF/views/admin/category/categoryEdit.jsp";
        }
    }

    // 폼 데이터로부터 CategoryDTO 생성
    private CategoryDTO parseCategoryDTOFromRequest(HttpServletRequest request) {
        CategoryDTO dto = new CategoryDTO();

        // 이름, 전체 이름, 설명
        dto.setName(request.getParameter("name"));
        dto.setFullName(request.getParameter("fullName"));
        dto.setDescription(request.getParameter("description"));

        // 상위 카테고리
        String parentIdStr = request.getParameter("parentId");
        if (parentIdStr != null && !parentIdStr.isEmpty()) {
            dto.setParentId(Integer.parseInt(parentIdStr));
        }

        // 순서
        String orderStr = request.getParameter("order");
        if (orderStr != null && !orderStr.isEmpty()) {
            dto.setOrder(Integer.parseInt(orderStr));
        }

        // 사용 여부
        String useYn = request.getParameter("useYn");
        dto.setUseYn(useYn != null ? useYn : "N");

        // 등록자 ID 설정
        String userId = null;
        if (request.getSession().getAttribute("user") != null) {
            userId = ((User) request.getSession().getAttribute("user")).getUserId();
        } else {
            userId = "admin"; // 기본값
        }
        dto.setRegisterId(userId);

        return dto;
    }
}