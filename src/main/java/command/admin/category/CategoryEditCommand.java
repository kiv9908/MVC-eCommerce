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
import java.util.stream.Collectors;

@Slf4j
public class CategoryEditCommand implements Command {
    private CategoryService categoryService;

    public CategoryEditCommand() {
        // 서비스 초기화
        AppConfig appConfig = AppConfig.getInstance();
        this.categoryService = appConfig.getCategoryService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();
        String pathInfo = request.getPathInfo();

        if ("GET".equals(method)) {
            // GET 요청 처리 - 수정 폼 표시
            return handleGetRequest(request, response);
        } else if ("POST".equals(method)) {
            // POST 요청 처리 - 수정 처리
            return handlePostRequest(request, response);
        }

        // 지원하지 않는 HTTP 메소드
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return null;
    }

    private String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            log.info("pathInfo: {}", pathInfo);

            // pathInfo 검증
            if (pathInfo == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "카테고리 ID가 없습니다.");
                return null;
            }

            // ID 추출 - 패턴: /edit/123 또는 /6 에서 숫자 부분 추출
            String categoryIdStr;
            if (pathInfo.contains("/edit/")) {
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

            int categoryId = Integer.parseInt(categoryIdStr);
            log.info("categoryId : {}", categoryId);

            // 수정할 카테고리 정보 조회 (DTO 사용)
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(categoryId);
            if (categoryDTO == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "카테고리를 찾을 수 없습니다.");
                return null;
            }

            // 상위 카테고리 선택을 위한 카테고리 목록 조회 (자기 자신은 제외)
            List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs().stream()
                    .filter(dto -> !dto.getId().equals(categoryId))
                    .collect(Collectors.toList());

            request.setAttribute("category", categoryDTO);
            request.setAttribute("parentCategories", parentCategoryDTOs);

            return "/WEB-INF/views/admin/category/categoryEdit.jsp";
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
            return null;
        } catch (Exception e) {
            log.info("카테고리 수정 폼 표시 중 오류 발생: {}", e.getMessage());
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
                List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs().stream()
                        .filter(dto -> !dto.getId().equals(categoryDTO.getId()))
                        .collect(Collectors.toList());
                request.setAttribute("parentCategories", parentCategoryDTOs);

                return "/WEB-INF/views/admin/category/categoryEdit.jsp";
            }

            // 카테고리 업데이트
            boolean success = categoryService.updateCategory(categoryDTO);

            if (success) {
                return "redirect:" + request.getContextPath() + "/admin/category/list?success=update";
            } else {
                request.setAttribute("errorMessage", "카테고리 수정에 실패했습니다.");
                request.setAttribute("category", categoryDTO);

                // 상위 카테고리 목록 가져오기 (폼 재표시용)
                List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs().stream()
                        .filter(dto -> !dto.getId().equals(categoryDTO.getId()))
                        .collect(Collectors.toList());
                request.setAttribute("parentCategories", parentCategoryDTOs);

                return "/WEB-INF/views/admin/category/categoryEdit.jsp";
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
            return null;
        } catch (Exception e) {
            log.info("카테고리 수정 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 수정 중 오류가 발생했습니다: " + e.getMessage());

            try {
                // 폼 데이터로부터 DTO 생성 (에러 메시지 표시용)
                CategoryDTO categoryDTO = parseCategoryDTOFromRequest(request);
                request.setAttribute("category", categoryDTO);

                // 상위 카테고리 목록 가져오기 (폼 재표시용)
                List<CategoryDTO> parentCategoryDTOs = categoryService.getAllCategoryDTOs().stream()
                        .filter(dto -> !dto.getId().equals(categoryDTO.getId()))
                        .collect(Collectors.toList());
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

        // categoryId 파라미터가 있는 경우 (수정 시)
        String categoryIdStr = request.getParameter("categoryId");
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            dto.setId(Integer.parseInt(categoryIdStr));
        }

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