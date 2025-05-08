package command.admin.mapping;

import config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import service.MappingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Slf4j
public class MappingDeleteCommand implements command.Command {

    private MappingService mappingService;

    public MappingDeleteCommand() {
        // 서비스 초기화
        AppConfig appConfig = AppConfig.getInstance();
        this.mappingService = appConfig.getMappingService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // URL에서 매핑 정보 추출
            String pathInfo = request.getPathInfo();
            // URL 형식: /delete/productCode/categoryId
            String[] pathParts = pathInfo.split("/");
            log.info("pathParts: {}", pathParts[0],pathParts[1],pathParts[2],pathParts[3]);
            if (pathParts.length != 4) {
                request.setAttribute("errorMessage", "잘못된 요청 형식입니다.");
                return"/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }

            String productCode = pathParts[2];
            Long categoryId = Long.parseLong(pathParts[3]);

            // 매핑 삭제
            boolean success = mappingService.deleteMappingByProductAndCategory(productCode, categoryId);

            if (success) {
                return "redirect:" + request.getContextPath() + "/admin/mapping/list?success=delete";
            } else {
                return "redirect:" + request.getContextPath() + "/admin/mapping/list?error=delete";
            }
        } catch (Exception e) {
            log.error("카테고리 매핑 삭제 중 오류 발생: {}", e.getMessage(), e);
            return "redirect:" + request.getContextPath() + "/admin/mapping/list?error=delete";
        }
    }
}
