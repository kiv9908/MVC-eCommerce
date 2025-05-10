package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;
import service.ProductService;
import domain.dao.ProductDAO;
import domain.dao.ProductDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProductListCommand implements Command {
    private final ProductService productService;
    private final FileService fileService;

    public ProductListCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.productService = appConfig.getProductService();
        this.fileService = appConfig.getFileService();

        // fileService가 null이면 로그 남기기
        if (this.fileService == null) {
            log.error("FileService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("상품 목록 조회 실행");

        try {
            // 페이지네이션 파라미터 처리
            int currentPage = 1;
            int pageSize = 10; // 페이지당 상품 수

            if (request.getParameter("page") != null) {
                try {
                    currentPage = Integer.parseInt(request.getParameter("page"));
                    if (currentPage < 1) {
                        currentPage = 1;
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 페이지 번호: {}", request.getParameter("page"));
                }
            }

            // 정렬 옵션 처리
            String sortBy = request.getParameter("sortBy");

            // 검색 기능
            String searchKeyword = request.getParameter("keyword");

            List<ProductDTO> pagedProducts;
            int totalCount;
            int totalPages;

            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                // 검색 결과에 대한 페이지네이션
                totalCount = productService.getSearchResultCount(searchKeyword);

                if ("priceAsc".equals(sortBy)) {
                    // 가격 오름차순 정렬 + 검색
                    pagedProducts = productService.searchProductDTOsOrderByPriceWithPagination(searchKeyword, true, currentPage, pageSize);
                } else if ("priceDesc".equals(sortBy)) {
                    // 가격 내림차순 정렬 + 검색
                    pagedProducts = productService.searchProductDTOsOrderByPriceWithPagination(searchKeyword, false, currentPage, pageSize);
                } else {
                    // 기본 검색 결과
                    pagedProducts = productService.searchProductDTOsWithPagination(searchKeyword, currentPage, pageSize);
                }

                request.setAttribute("searchResults", pagedProducts);
                request.setAttribute("searchKeyword", searchKeyword);
            } else {
                // 전체 상품에 대한 페이지네이션
                totalCount = productService.getTotalProductCount();

                if ("priceAsc".equals(sortBy)) {
                    pagedProducts = productService.getProductDTOsOrderByPriceWithPagination(true, currentPage, pageSize);
                } else if ("priceDesc".equals(sortBy)) {
                    pagedProducts = productService.getProductDTOsOrderByPriceWithPagination(false, currentPage, pageSize);
                } else {
                    pagedProducts = productService.getProductDTOsWithPagination(currentPage, pageSize);
                }

                request.setAttribute("products", pagedProducts);
            }

            // 총 페이지 수 계산
            totalPages = (int) Math.ceil((double) totalCount / pageSize);
            int startPage = Math.max(1, currentPage - 2);
            int endPage = Math.min(totalPages, startPage + 4);
            startPage = Math.max(1, endPage - 4);

            // 페이지네이션 정보 설정
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("startPage", startPage);
            request.setAttribute("endPage", endPage);
            // 정렬 옵션 저장 - 페이지 템플릿에서 사용하기 위해
            request.setAttribute("sortBy", sortBy);

            return "/WEB-INF/views/admin/product/productList.jsp";
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "상품 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }

    }
}