package command.admin.mapping;

import config.AppConfig;
import domain.dto.MappingDTO;
import lombok.extern.slf4j.Slf4j;
import service.MappingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class MappingEditCommand implements command.Command {
    private MappingService mappingService;

    public MappingEditCommand(){
        AppConfig appConfig = AppConfig.getInstance();
        this.mappingService = appConfig.getMappingService();
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

            String productCode = null;
            Long categoryId = null;

            if (pathInfo != null && pathInfo.startsWith("/edit/")) {
                String[] parts = pathInfo.substring("/edit/".length()).split("/");

                if (parts.length >= 2) {
                    productCode = parts[0];   // 첫 번째 숫자
                    categoryId = Long.parseLong(parts[1]);    // 두 번째 숫자
                } else {
                    // 경로 형식이 잘못된 경우에 대한 예외 처리 (선택 사항)
                    throw new IllegalArgumentException("URL 형식이 올바르지 않습니다. 예: /edit/{productCode}/{categoryId}");
                }
            }

            log.info("productCode: {}, categoryId: {}", productCode, categoryId);

            // 수정할 매핑 정보 조회
            MappingDTO mappingDTO = mappingService.getMappingByProductAndCategory(productCode, categoryId);
            if (mappingDTO == null) {
                request.setAttribute("errorMessage", "카테고리 매핑을 찾을 수 없습니다.");
                return "/WEB-INF/views/common/error.jsp";
            }
            request.setAttribute("mapping", mappingDTO);

            // 카테고리 목록 가져오기
            request.setAttribute("categories", mappingService.getAllCategories());

            // 상품 목록 가져오기
            request.setAttribute("products", mappingService.getAllProducts());

            // 통합된 매핑 페이지로 포워딩
            return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 카테고리 ID 형식입니다.");
            return null;
        } catch (Exception e) {
            log.info("카테고리 수정 폼 표시 중 오류 발생: {}", e.getMessage());
            request.setAttribute("errorMessage", "카테고리 매핑 폼을 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/common/error.jsp";
        }
    }

    private String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // 원래 매핑 정보 (히든 필드에서 추출)
            String originalProductCode = request.getParameter("originalProductCode");
            Long originalCategoryId = Long.parseLong(request.getParameter("originalCategoryId"));

            // 새 매핑 정보
            MappingDTO mappingDTO = new MappingDTO();
            mappingDTO.setId(Long.parseLong(request.getParameter("id")));
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

            log.info("Original mapping: [{}:{}], New mapping: {}", originalProductCode, originalCategoryId, mappingDTO);

            // 유효성 검증
            if (mappingDTO.getProductCode() == null || mappingDTO.getProductCode().isEmpty() || mappingDTO.getCategoryId() == null) {
                request.setAttribute("errorMessage", "상품코드와 카테고리 ID는 필수 입력 항목입니다.");
                request.setAttribute("mapping", mappingDTO);
                request.setAttribute("categories", mappingService.getAllCategories());
                request.setAttribute("products", mappingService.getAllProducts());
                return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }

            // 새 매핑이 이미 존재하는지 확인
            if (!originalProductCode.equals(mappingDTO.getProductCode()) || !originalCategoryId.equals(mappingDTO.getCategoryId())) {
                MappingDTO existingMapping = mappingService.getMappingByProductAndCategory(
                        mappingDTO.getProductCode(), mappingDTO.getCategoryId());

                if (existingMapping != null) {
                    request.setAttribute("errorMessage", "이미 동일한 상품과 카테고리 매핑이 존재합니다.");
                    request.setAttribute("mapping", mappingDTO);
                    request.setAttribute("categories", mappingService.getAllCategories());
                    request.setAttribute("products", mappingService.getAllProducts());
                    return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
                }
            }

            // 1단계: 기존 매핑 삭제
            boolean deleteSuccess = mappingService.deleteMappingByProductAndCategory(originalProductCode, originalCategoryId);

            if (!deleteSuccess) {
                log.error("기존 매핑 삭제 실패: [{}:{}]", originalProductCode, originalCategoryId);
                request.setAttribute("errorMessage", "기존 카테고리 매핑 삭제에 실패했습니다.");
                request.setAttribute("mapping", mappingDTO);
                request.setAttribute("categories", mappingService.getAllCategories());
                request.setAttribute("products", mappingService.getAllProducts());
                return "/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }

            // 2단계: 새 매핑 생성
            boolean createSuccess = mappingService.createMapping(mappingDTO);

            if (createSuccess) {
                return "redirect:" + request.getContextPath() + "/admin/mapping/list?success=update";
            } else {
                // 실패 시 원래 매핑 복원 시도
                log.warn("새 매핑 생성 실패: {}, 원래 매핑 복원 시도...", mappingDTO);
                MappingDTO originalMapping = new MappingDTO();
                originalMapping.setProductCode(originalProductCode);
                originalMapping.setCategoryId(originalCategoryId);
                mappingService.createMapping(originalMapping);

                request.setAttribute("errorMessage", "카테고리 매핑 수정에 실패했습니다.");
                request.setAttribute("mapping", mappingDTO);
                request.setAttribute("categories", mappingService.getAllCategories());
                request.setAttribute("products", mappingService.getAllProducts());
                return"/WEB-INF/views/admin/mapping/mappingEdit.jsp";
            }
        } catch (Exception e) {
            log.error("카테고리 매핑 수정 중 오류 발생: {}", e.getMessage(), e);
            request.setAttribute("errorMessage", "카테고리 매핑 수정 중 오류가 발생했습니다: " + e.getMessage());
            return"/WEB-INF/views/admin/mapping/mappingEdit.jsp";
        }
    }
}
