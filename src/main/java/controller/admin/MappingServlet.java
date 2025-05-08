//package controller.admin;
//
//import domain.dao.MappingDAO;
//import domain.dao.MappingDAOImpl;
//import domain.dto.MappingDTO;
//import lombok.extern.slf4j.Slf4j;
//import service.MappingService;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.List;
//
//@Slf4j
//@WebServlet("/admin/mapping/*")
//public class MappingServlet extends HttpServlet {
//
//    private MappingService mappingService;
//
//    @Override
//    public void init() throws ServletException {
//        // DAO와 서비스 초기화
//        MappingDAO mappingDAO = new MappingDAOImpl();
//        mappingService = new MappingService(mappingDAO);
//    }
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String pathInfo = request.getPathInfo();
//
//        if (pathInfo == null || "/".equals(pathInfo) || "/list".equals(pathInfo)) {
//            // 카테고리 매핑 목록 페이지
//            listMappings(request, response);
//        } else if ("/create".equals(pathInfo)) {
//            // 카테고리 매핑 생성 페이지
//            showMappingForm(request, response, true);
//        } else if (pathInfo.startsWith("/edit/")) {
//            // 카테고리 매핑 수정 페이지
//            showMappingForm(request, response, false);
//        } else if (pathInfo.startsWith("/delete/")) {
//            // 카테고리 매핑 삭제 처리
//            deleteMapping(request, response);
//        } else {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String pathInfo = request.getPathInfo();
//
//        if ("/create".equals(pathInfo)) {
//            // 카테고리 매핑 생성 처리
//            createMapping(request, response);
//        } else if ("/edit".equals(pathInfo)) {
//            // 카테고리 매핑 수정 처리
//            updateMapping(request, response);
//        } else {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//        }
//    }
//
//    // 카테고리 매핑 목록 표시
//    private void listMappings(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try {
//            List<MappingDTO> mappingDTOs = mappingService.getAllMappings();
//            request.setAttribute("mappings", mappingDTOs);
//
//            // 검색 기능
//            String searchKeyword = request.getParameter("keyword");
//            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
//                List<MappingDTO> searchResults = mappingService.searchMappings(searchKeyword);
//                request.setAttribute("searchResults", searchResults);
//                request.setAttribute("searchKeyword", searchKeyword);
//            }
//
//            // 카테고리 매핑 목록 페이지로 포워딩
//            request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingList.jsp").forward(request, response);
//        } catch (Exception e) {
//            log.error("카테고리 매핑 목록 조회 중 오류 발생: {}", e.getMessage(), e);
//            request.setAttribute("errorMessage", "카테고리 매핑 목록을 불러오는 중 오류가 발생했습니다.");
//            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
//        }
//    }
//
//    // 폼 표시 (생성 및 수정)
//    private void showMappingForm(HttpServletRequest request, HttpServletResponse response, boolean isCreate) throws ServletException, IOException {
//        try {
//            if (isCreate) {
//                // 빈 DTO 생성하여 폼에 전달 (생성 모드)
//                MappingDTO emptyDTO = new MappingDTO();
//                request.setAttribute("mapping", emptyDTO);
//            } else {
//                // URL에서 매핑 정보 추출 (수정 모드)
//                String pathInfo = request.getPathInfo();
//                // URL 형식: /edit/productCode/categoryId (예: /edit/P001/123)
//                String[] pathParts = pathInfo.split("/");
//                if (pathParts.length != 4) {
//                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청 형식입니다.");
//                    return;
//                }
//
//                String productCode = pathParts[2];
//                Long categoryId = Long.parseLong(pathParts[3]);
//
//                // 수정할 매핑 정보 조회
//                MappingDTO mappingDTO = mappingService.getMappingByProductAndCategory(productCode, categoryId);
//                if (mappingDTO == null) {
//                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "카테고리 매핑을 찾을 수 없습니다.");
//                    return;
//                }
//
//                request.setAttribute("mapping", mappingDTO);
//            }
//
//            // 카테고리 목록 가져오기
//            request.setAttribute("categories", mappingService.getAllCategories());
//
//            // 상품 목록 가져오기
//            request.setAttribute("products", mappingService.getAllProducts());
//
//            // 통합된 매핑 페이지로 포워딩
//            request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//        } catch (Exception e) {
//            String operation = isCreate ? "생성" : "수정";
//            log.error("카테고리 매핑 {} 폼 표시 중 오류 발생: {}", operation, e.getMessage(), e);
//            request.setAttribute("errorMessage", "카테고리 매핑 " + operation + " 폼을 불러오는 중 오류가 발생했습니다.");
//            request.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(request, response);
//        }
//    }
//
//    // 카테고리 매핑 생성 처리
//    private void createMapping(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.setCharacterEncoding("UTF-8");
//
//        try {
//            // 폼 데이터 파싱
//            MappingDTO mappingDTO = new MappingDTO();
//            mappingDTO.setProductCode(request.getParameter("productCode"));
//            mappingDTO.setCategoryId(Long.parseLong(request.getParameter("categoryId")));
//
//            // 추가 필드 파싱
//            String displayOrderStr = request.getParameter("displayOrder");
//            if (displayOrderStr != null && !displayOrderStr.isEmpty()) {
//                mappingDTO.setDisplayOrder(Integer.parseInt(displayOrderStr));
//            } else {
//                mappingDTO.setDisplayOrder(1); // 기본값
//            }
//
//            String registerUser = request.getParameter("registerUser");
//            if (registerUser != null && !registerUser.isEmpty()) {
//                mappingDTO.setRegisterUser(registerUser);
//            } else {
//                // 세션에서 현재 로그인한 사용자 정보 가져오기
//                String userName = (String) request.getSession().getAttribute("adminId");
//                mappingDTO.setRegisterUser(userName != null ? userName : "SYSTEM");
//            }
//
//            // 유효성 검증
//            if (mappingDTO.getProductCode() == null || mappingDTO.getProductCode().isEmpty() || mappingDTO.getCategoryId() == null) {
//                request.setAttribute("errorMessage", "상품코드와 카테고리 ID는 필수 입력 항목입니다.");
//                request.setAttribute("mapping", mappingDTO);
//                request.setAttribute("categories", mappingService.getAllCategories());
//                request.setAttribute("products", mappingService.getAllProducts());
//                request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//                return;
//            }
//
//            // 중복 매핑 확인
//            MappingDTO existingMapping = mappingService.getMappingByProductAndCategory(
//                    mappingDTO.getProductCode(), mappingDTO.getCategoryId());
//
//            if (existingMapping != null) {
//                request.setAttribute("errorMessage", "이미 동일한 상품과 카테고리 매핑이 존재합니다.");
//                request.setAttribute("mapping", mappingDTO);
//                request.setAttribute("categories", mappingService.getAllCategories());
//                request.setAttribute("products", mappingService.getAllProducts());
//                request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//                return;
//            }
//
//            // 매핑 저장
//            boolean success = mappingService.createMapping(mappingDTO);
//
//            if (success) {
//                response.sendRedirect(request.getContextPath() + "/admin/mapping/list?success=create");
//            } else {
//                request.setAttribute("errorMessage", "카테고리 매핑 생성에 실패했습니다.");
//                request.setAttribute("mapping", mappingDTO);
//                request.setAttribute("categories", mappingService.getAllCategories());
//                request.setAttribute("products", mappingService.getAllProducts());
//                request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//            }
//        } catch (Exception e) {
//            log.error("카테고리 매핑 생성 중 오류 발생: {}", e.getMessage(), e);
//            request.setAttribute("errorMessage", "카테고리 매핑 생성 중 오류가 발생했습니다: " + e.getMessage());
//            request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//        }
//    }
//
//    // 카테고리 매핑 수정 처리
//    private void updateMapping(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.setCharacterEncoding("UTF-8");
//
//        try {
//            // 원래 매핑 정보 (히든 필드에서 추출)
//            String originalProductCode = request.getParameter("originalProductCode");
//            Long originalCategoryId = Long.parseLong(request.getParameter("originalCategoryId"));
//
//            // 새 매핑 정보
//            MappingDTO mappingDTO = new MappingDTO();
//            mappingDTO.setId(Long.parseLong(request.getParameter("id")));
//            mappingDTO.setProductCode(request.getParameter("productCode"));
//            mappingDTO.setCategoryId(Long.parseLong(request.getParameter("categoryId")));
//
//            // 추가 필드 파싱
//            String displayOrderStr = request.getParameter("displayOrder");
//            if (displayOrderStr != null && !displayOrderStr.isEmpty()) {
//                mappingDTO.setDisplayOrder(Integer.parseInt(displayOrderStr));
//            } else {
//                mappingDTO.setDisplayOrder(1); // 기본값
//            }
//
//            String registerUser = request.getParameter("registerUser");
//            if (registerUser != null && !registerUser.isEmpty()) {
//                mappingDTO.setRegisterUser(registerUser);
//            } else {
//                // 세션에서 현재 로그인한 사용자 정보 가져오기
//                String userName = (String) request.getSession().getAttribute("adminId");
//                mappingDTO.setRegisterUser(userName != null ? userName : "SYSTEM");
//            }
//
//            log.info("Original mapping: [{}:{}], New mapping: {}", originalProductCode, originalCategoryId, mappingDTO);
//
//            // 유효성 검증
//            if (mappingDTO.getProductCode() == null || mappingDTO.getProductCode().isEmpty() || mappingDTO.getCategoryId() == null) {
//                request.setAttribute("errorMessage", "상품코드와 카테고리 ID는 필수 입력 항목입니다.");
//                request.setAttribute("mapping", mappingDTO);
//                request.setAttribute("categories", mappingService.getAllCategories());
//                request.setAttribute("products", mappingService.getAllProducts());
//                request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//                return;
//            }
//
//            // 새 매핑이 이미 존재하는지 확인
//            if (!originalProductCode.equals(mappingDTO.getProductCode()) || !originalCategoryId.equals(mappingDTO.getCategoryId())) {
//                MappingDTO existingMapping = mappingService.getMappingByProductAndCategory(
//                        mappingDTO.getProductCode(), mappingDTO.getCategoryId());
//
//                if (existingMapping != null) {
//                    request.setAttribute("errorMessage", "이미 동일한 상품과 카테고리 매핑이 존재합니다.");
//                    request.setAttribute("mapping", mappingDTO);
//                    request.setAttribute("categories", mappingService.getAllCategories());
//                    request.setAttribute("products", mappingService.getAllProducts());
//                    request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//                    return;
//                }
//            }
//
//            // 1단계: 기존 매핑 삭제
//            boolean deleteSuccess = mappingService.deleteMappingByProductAndCategory(originalProductCode, originalCategoryId);
//
//            if (!deleteSuccess) {
//                log.error("기존 매핑 삭제 실패: [{}:{}]", originalProductCode, originalCategoryId);
//                request.setAttribute("errorMessage", "기존 카테고리 매핑 삭제에 실패했습니다.");
//                request.setAttribute("mapping", mappingDTO);
//                request.setAttribute("categories", mappingService.getAllCategories());
//                request.setAttribute("products", mappingService.getAllProducts());
//                request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//                return;
//            }
//
//            // 2단계: 새 매핑 생성
//            boolean createSuccess = mappingService.createMapping(mappingDTO);
//
//            if (createSuccess) {
//                response.sendRedirect(request.getContextPath() + "/admin/mapping/list?success=update");
//            } else {
//                // 실패 시 원래 매핑 복원 시도
//                log.warn("새 매핑 생성 실패: {}, 원래 매핑 복원 시도...", mappingDTO);
//                MappingDTO originalMapping = new MappingDTO();
//                originalMapping.setProductCode(originalProductCode);
//                originalMapping.setCategoryId(originalCategoryId);
//                mappingService.createMapping(originalMapping);
//
//                request.setAttribute("errorMessage", "카테고리 매핑 수정에 실패했습니다.");
//                request.setAttribute("mapping", mappingDTO);
//                request.setAttribute("categories", mappingService.getAllCategories());
//                request.setAttribute("products", mappingService.getAllProducts());
//                request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//            }
//        } catch (Exception e) {
//            log.error("카테고리 매핑 수정 중 오류 발생: {}", e.getMessage(), e);
//            request.setAttribute("errorMessage", "카테고리 매핑 수정 중 오류가 발생했습니다: " + e.getMessage());
//            request.getRequestDispatcher("/WEB-INF/views/admin/mapping/mappingEdit.jsp").forward(request, response);
//        }
//    }
//
//    // 카테고리 매핑 삭제 처리
//    private void deleteMapping(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try {
//            // URL에서 매핑 정보 추출
//            String pathInfo = request.getPathInfo();
//            // URL 형식: /delete/productCode/categoryId (예: /delete/P001/123)
//            String[] pathParts = pathInfo.split("/");
//            if (pathParts.length != 4) {
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청 형식입니다.");
//                return;
//            }
//
//            String productCode = pathParts[2];
//            Long categoryId = Long.parseLong(pathParts[3]);
//
//            // 매핑 삭제
//            boolean success = mappingService.deleteMappingByProductAndCategory(productCode, categoryId);
//
//            if (success) {
//                response.sendRedirect(request.getContextPath() + "/admin/mapping/list?success=delete");
//            } else {
//                response.sendRedirect(request.getContextPath() + "/admin/mapping/list?error=delete");
//            }
//        } catch (Exception e) {
//            log.error("카테고리 매핑 삭제 중 오류 발생: {}", e.getMessage(), e);
//            response.sendRedirect(request.getContextPath() + "/admin/mapping/list?error=delete");
//        }
//    }
//}
