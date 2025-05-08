package command.admin.mapping;

import command.Command;
import config.AppConfig;
import domain.dao.CategoryDAO;
import domain.dao.CategoryDAOImpl;
import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;
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
            List<MappingDTO> mappingDTOs = mappingService.getAllMappings();
            request.setAttribute("mappings", mappingDTOs);

            // 검색 기능
            String searchKeyword = request.getParameter("keyword");
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                List<MappingDTO> searchResults = mappingService.searchMappings(searchKeyword);
                request.setAttribute("searchResults", searchResults);
                request.setAttribute("searchKeyword", searchKeyword);
            }
            return "/WEB-INF/views/admin/mapping/mappingList.jsp";
        } catch (Exception e) {
            log.error("카테고리 매핑 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "카테고리 매핑 목록을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }
}