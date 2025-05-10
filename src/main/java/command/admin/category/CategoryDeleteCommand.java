package command.admin.category;

import command.Command;
import config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import service.CategoryService;
import domain.dao.CategoryDAO;
import domain.dao.CategoryDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CategoryDeleteCommand implements Command {
    private CategoryService categoryService;

    public CategoryDeleteCommand() {
        // 서비스 초기화
        AppConfig appConfig = AppConfig.getInstance();
        this.categoryService = appConfig.getCategoryService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL에서 카테고리 ID 추출
            String pathInfo = request.getPathInfo();
            // ID 추출 - 패턴: /edit/123 또는 /6 에서 숫자 부분 추출
            String categoryIdStr;
            if (pathInfo.contains("/delete/")) {
                // URL에 /edit/가 포함된 경우 - /edit/ 다음 부분 추출
                categoryIdStr = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
            } else if (pathInfo.startsWith("/")) {
                // 시작이 /인 경우 - 슬래시 제거
                categoryIdStr = pathInfo.substring(1);
            } else {
                // 그 외의 경우 그대로 사용
                categoryIdStr = pathInfo;
            }

            log.info("categoryIdStr: {}", categoryIdStr);

            Long categoryId = Long.parseLong(categoryIdStr);
            log.info("categoryId : {}", categoryId);


            // 카테고리 삭제
            boolean success = categoryService.deleteCategory(categoryId);

            if (success) {
                return "redirect:" + request.getContextPath() + "/admin/category/list?success=delete";
            } else {
                return "redirect:" + request.getContextPath() + "/admin/category/list?error=delete";
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
            return null;
        } catch (Exception e) {
            log.info("카테고리 삭제 중 오류 발생: {}", e.getMessage());
            return "redirect:" + request.getContextPath() + "/admin/category/list?error=delete";
        }
    }
}