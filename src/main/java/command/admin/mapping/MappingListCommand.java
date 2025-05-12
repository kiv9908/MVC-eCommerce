package command.admin.mapping;

import command.Command;
import config.AppConfig;
import domain.dao.CategoryDAO;
import domain.dao.CategoryDAOImpl;
import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;
import domain.dto.PageDTO;
import lombok.extern.slf4j.Slf4j;
import service.CategoryService;
import service.MappingService;
import domain.dao.MappingDAO;
import domain.dao.MappingDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class MappingListCommand implements Command {
    private final MappingService mappingService;

    public MappingListCommand() {
        // 서비스 초기화
        AppConfig appConfig = AppConfig.getInstance();
        this.mappingService = appConfig.getMappingService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("카테고리 매핑 목록 조회 실행");

        try {
            // 페이지네이션 처리
            PageDTO pageDTO = new PageDTO();

            // 현재 페이지 설정
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                pageDTO.setCurrentPage(Integer.parseInt(pageParam));
            }

            // 페이지당 항목 수 설정
            String pageSizeParam = request.getParameter("pageSize");
            if (pageSizeParam != null && !pageSizeParam.isEmpty()) {
                pageDTO.setPageSize(Integer.parseInt(pageSizeParam));
            }

            // 정렬 설정
            String sortBy = request.getParameter("sortBy");
            if (sortBy != null && !sortBy.isEmpty()) {
                pageDTO.setSortBy(sortBy);
            }

            // 검색 기능
            String searchKeyword = request.getParameter("keyword");
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                pageDTO.setKeyword(searchKeyword);

                // 키워드로 검색 + 페이지네이션
                List<MappingDTO> mappings = mappingService.searchMappingsWithPagination(searchKeyword, pageDTO);
                request.setAttribute("mappings", mappings);
                request.setAttribute("searchKeyword", searchKeyword);
            } else {
                // 전체 목록 + 페이지네이션
                List<MappingDTO> mappings = mappingService.getMappingsWithPagination(pageDTO);
                request.setAttribute("mappings", mappings);
            }

            // 페이지 정보 설정
            request.setAttribute("pageDTO", pageDTO);

            return "/WEB-INF/views/admin/mapping/mappingList.jsp";
        } catch (Exception e) {
            log.error("카테고리 매핑 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "카테고리 매핑 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}