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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
            String categoryIdStr;
            if (pathInfo.contains("/delete/")) {
                categoryIdStr = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
            } else if (pathInfo.startsWith("/")) {
                categoryIdStr = pathInfo.substring(1);
            } else {
                categoryIdStr = pathInfo;
            }

            log.info("categoryIdStr: {}", categoryIdStr);
            Long categoryId = Long.parseLong(categoryIdStr);
            log.info("categoryId : {}", categoryId);

            // 페이지 정보 가져오기
            String page = request.getParameter("page");
            String keyword = request.getParameter("keyword");

            // 카테고리 삭제
            boolean success = categoryService.deleteCategory(categoryId);

            // 리다이렉트 URL 구성
            StringBuilder redirectUrlBuilder = new StringBuilder();
            redirectUrlBuilder.append("redirect:").append(request.getContextPath())
                    .append("/admin/category/list?")
                    .append(success ? "success=delete" : "error=delete");

            // 페이지 정보 추가
            if (page != null && !page.isEmpty()) {
                redirectUrlBuilder.append("&page=").append(page);
            }

            // 검색어 추가
            if (keyword != null && !keyword.isEmpty()) {
                try {
                    String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
                    redirectUrlBuilder.append("&keyword=").append(encodedKeyword);
                } catch (UnsupportedEncodingException e) {
                    log.error("키워드 인코딩 오류", e);
                }
            }

            return redirectUrlBuilder.toString();
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
            return null;
        } catch (Exception e) {
            log.error("카테고리 삭제 중 오류 발생: {}", e.getMessage(), e);
            return "redirect:" + request.getContextPath() + "/admin/category/list?error=delete";
        }
    }
}