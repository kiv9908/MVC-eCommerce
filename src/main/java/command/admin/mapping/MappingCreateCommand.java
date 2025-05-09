package command.admin.mapping;

import command.Command;
import config.AppConfig;
import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;

import domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import service.MappingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class MappingCreateCommand implements Command {
    private MappingService mappingService;

    public MappingCreateCommand() {
        AppConfig appConfig = AppConfig.getInstance();
        this.mappingService = appConfig.getMappingService();
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();

        if ("GET".equals(method)) {
            // GET 요청 처리 - 생성 폼 표시
            return handleGetRequest(request, response);
        } else if ("POST".equals(method)) {
            // POST 요청 처리 - 생성 처리
            return handlePostRequest(request, response);
        }

        // 지원하지 않는 HTTP 메소드
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return null;

    }

    private String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 빈 DTO 생성하여 폼에 전달 (생성 모드)
            MappingDTO emptyDTO = new MappingDTO();
            request.setAttribute("mapping", emptyDTO);

            // 카테고리 목록 가져오기
            request.setAttribute("categories", mappingService.getAllCategories());

            // 상품 목록 가져오기
            request.setAttribute("products", mappingService.getAllProducts());

            return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
        } catch (Exception e) {
            log.error("카테고리 매핑 {} 폼 표시 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "카테고리 매핑 폼을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }

    private String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // 폼 데이터 파싱
            MappingDTO mappingDTO = new MappingDTO();
            mappingDTO.setProductCode(request.getParameter("productCode"));
            mappingDTO.setCategoryId(Long.parseLong(request.getParameter("categoryId")));

            // 추가 필드 파싱
            String displayOrderStr = request.getParameter("displayOrder");
            if (displayOrderStr != null && !displayOrderStr.isEmpty()) {
                mappingDTO.setDisplayOrder(Integer.parseInt(displayOrderStr));
            } else {
                mappingDTO.setDisplayOrder(1); // 기본값
            }

            String registerUser = request.getParameter("registerUser");
            if (registerUser != null && !registerUser.isEmpty()) {
                mappingDTO.setRegisterUser(registerUser);
            } else {
                // 세션에서 현재 로그인한 사용자 정보 가져오기
                String userName = (String) request.getSession().getAttribute("adminId");
                mappingDTO.setRegisterUser(userName != null ? userName : "SYSTEM");
            }

            // 유효성 검증
            if (mappingDTO.getProductCode() == null || mappingDTO.getProductCode().isEmpty() || mappingDTO.getCategoryId() == null) {
                request.setAttribute("errorMessage", "상품코드와 카테고리 ID는 필수 입력 항목입니다.");
                request.setAttribute("mapping", mappingDTO);
                request.setAttribute("categories", mappingService.getAllCategories());
                request.setAttribute("products", mappingService.getAllProducts());
                return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }

            // 중복 매핑 확인
            MappingDTO existingMapping = mappingService.getMappingByProductAndCategory(
                    mappingDTO.getProductCode(), mappingDTO.getCategoryId());

            if (existingMapping != null) {
                request.setAttribute("errorMessage", "이미 동일한 상품과 카테고리 매핑이 존재합니다.");
                request.setAttribute("mapping", mappingDTO);
                request.setAttribute("categories", mappingService.getAllCategories());
                request.setAttribute("products", mappingService.getAllProducts());
                return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }

            // 매핑 저장
            boolean success = mappingService.createMapping(mappingDTO);

            if (success) {
                return "redirect:" + request.getContextPath() + "/admin/mapping/list?success=create";
            } else {
                request.setAttribute("errorMessage", "카테고리 매핑 생성에 실패했습니다.");
                request.setAttribute("mapping", mappingDTO);
                request.setAttribute("categories", mappingService.getAllCategories());
                request.setAttribute("products", mappingService.getAllProducts());
                return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }

        } catch (Exception e) {
            log.error("카테고리 매핑 생성 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "카테고리 매핑 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
        }
    }

    // 폼 데이터로부터 CategoryDTO 생성
    private CategoryDTO parseCategoryDTOFromRequest(HttpServletRequest request) {
        CategoryDTO dto = new CategoryDTO();

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
            userId = ((UserDTO) request.getSession().getAttribute("user")).getUserId();
        } else {
            userId = "admin"; // 기본값
        }
        dto.setRegisterId(userId);

        return dto;
    }
}