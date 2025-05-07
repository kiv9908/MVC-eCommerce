package command.admin.category;

import command.Command;
import config.AppConfig;
import domain.dto.CategoryDTO;
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
public class CategoryListCommand implements Command {
    private CategoryService categoryService;

    public CategoryListCommand() {
        // 서비스 초기화
        AppConfig appConfig = AppConfig.getInstance();
        this.categoryService = appConfig.getCategoryService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("카테고리 목록 조회 실행");

        try {
            // 계층형 구조로 카테고리 조회 (DTO 사용)
            List<CategoryDTO> categoryDTOs = categoryService.getAllCategoryDTOs();
            request.setAttribute("categories", categoryDTOs);

            // (선택) 검색 기능이 있는 경우
            String searchKeyword = request.getParameter("keyword");
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                List<CategoryDTO> searchResults = categoryService.searchCategoryDTOs(searchKeyword);
                request.setAttribute("searchResults", searchResults);
                request.setAttribute("searchKeyword", searchKeyword);
            }

            return "/WEB-INF/views/admin/category/categoryList.jsp";
        } catch (Exception e) {
            log.info("카테고리 목록 조회 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}