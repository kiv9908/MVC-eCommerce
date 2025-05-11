package command.admin.category;

import command.Command;
import config.AppConfig;
import domain.dto.CategoryDTO;
import domain.dto.PageDTO;
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
            // 요청 파라미터에서 PageDTO 생성
            PageDTO pageDTO = categoryService.createPageDTOFromParameters(
                    request.getParameter("page"),
                    request.getParameter("keyword")
            );

            // 페이지 크기 설정 (선택적으로 조정 가능)
            pageDTO.setPageSize(10);

            // 서비스 계층에서 페이지네이션 설정
            pageDTO = categoryService.setupCategoryPage(pageDTO);

            // 카테고리 목록 조회
            List<CategoryDTO> pagedCategories = categoryService.getCategoriesByPage(pageDTO);

            // 결과 저장
            if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().trim().isEmpty()) {
                request.setAttribute("searchResults", pagedCategories);
                request.setAttribute("searchKeyword", pageDTO.getKeyword());
            } else {
                request.setAttribute("categories", pagedCategories);
            }

            // PageDTO를 request에 저장
            request.setAttribute("pageDTO", pageDTO);

            return "/WEB-INF/views/admin/category/categoryList.jsp";
        } catch (Exception e) {
            log.error("카테고리 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "카테고리 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}