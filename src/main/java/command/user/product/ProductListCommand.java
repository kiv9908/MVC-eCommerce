package command.user.product;

import command.Command;
import domain.dto.CategoryDTO;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;
import service.CategoryService;
import service.ProductService;
import config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ProductListCommand implements Command {
    private ProductService productService;
    private CategoryService categoryService;

    public ProductListCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();
        this.categoryService = appConfig.getCategoryService();

        // 서비스 초기화 확인
        if (this.productService == null) {
            log.error("ProductService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }

        if (this.categoryService == null) {
            log.error("CategoryService가 초기화되지 않았습니다. AppConfig 설정을 확인해주세요.");
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 요청 파라미터에서 페이지 정보 추출
        String pageParam = request.getParameter("page");
        String sortByParam = request.getParameter("sortBy");
        String keywordParam = request.getParameter("keyword");

        // 카테고리 파라미터 처리
        String categoryIdParam = request.getParameter("categoryId");
        Long categoryId = null;
        if (categoryIdParam != null && !categoryIdParam.isEmpty()) {
            try {
                categoryId = Long.parseLong(categoryIdParam);
            } catch (NumberFormatException e) {
                // 잘못된 카테고리 ID 형식은 무시
            }
        }

        // PageDTO 객체 생성 및 초기화
        PageDTO pageDTO = productService.createPageDTOFromParameters(pageParam, sortByParam, keywordParam);

        // 사용자 상품 목록에서는 페이지 크기를 16으로 설정 (admin과 구분)
        pageDTO.setPageSize(16);

        // 페이지네이션 정보 계산 (카테고리 정보 포함)
        pageDTO = productService.setupProductPage(pageDTO, categoryId);

        // 현재 페이지에 해당하는 상품 목록 조회 (카테고리 정보 포함)
        List<ProductDTO> products = productService.getProductsByPage(pageDTO, categoryId);

        // 모든 카테고리 목록 조회 (사이드바 표시용)
        List<CategoryDTO> categories = categoryService.getAllCategoryDTOs();

        // 요청 속성 설정
        request.setAttribute("products", products);
        request.setAttribute("pageDTO", pageDTO);
        request.setAttribute("categories", categories);
        request.setAttribute("categoryId", categoryId);

        // JSP 페이지 경로 반환
        return "/WEB-INF/views/user/productList.jsp";
    }
}