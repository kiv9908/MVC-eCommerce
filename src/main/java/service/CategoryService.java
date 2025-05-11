package service;

import domain.dao.CategoryDAO;
import domain.dto.CategoryDTO;
import domain.dto.PageDTO;

import java.util.List;
import java.util.logging.Logger;

public class CategoryService {
    private static final Logger logger = Logger.getLogger(CategoryService.class.getName());

    private final CategoryDAO categoryDAO;

    // 생성자 주입
    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /**
     * 모든 카테고리 DTO 조회
     */
    public List<CategoryDTO> getAllCategoryDTOs() {
        return categoryDAO.findAll();
    }

    /**
     * 카테고리 ID로 카테고리 DTO 조회
     */
    public CategoryDTO getCategoryDTOById(Long categoryId) {
        return categoryDAO.findById(categoryId);
    }

    /**
     * 카테고리 전체 경로명과 레벨을 자동으로 설정
     * @param categoryDTO 설정할 카테고리 DTO
     * @return 설정이 완료된 카테고리 DTO
     */
    public CategoryDTO setFullCategoryNameAndLevel(CategoryDTO categoryDTO) {
        if (categoryDTO == null || categoryDTO.getName() == null) {
            return categoryDTO;
        }

        // 상위 카테고리가 있는 경우
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() > 0) {
            CategoryDTO parentCategory = categoryDAO.findById(categoryDTO.getParentId());
            if (parentCategory != null) {
                // 전체 카테고리명 설정
                String parentFullName = parentCategory.getFullName() != null && !parentCategory.getFullName().isEmpty()
                        ? parentCategory.getFullName()
                        : parentCategory.getName();
                categoryDTO.setFullName(parentFullName + " > " + categoryDTO.getName());

                // 레벨 설정 (부모 레벨 + 1)
                int parentLevel = parentCategory.getLevel() != null ? parentCategory.getLevel() : 0;
                categoryDTO.setLevel(parentLevel + 1);
            } else {
                // 상위 카테고리를 찾을 수 없는 경우, 최상위 카테고리로 취급
                categoryDTO.setFullName(categoryDTO.getName());
                categoryDTO.setLevel(0); // 대분류
            }
        } else {
            // 최상위 카테고리인 경우
            categoryDTO.setFullName(categoryDTO.getName());
            categoryDTO.setLevel(0); // 대분류
        }

        return categoryDTO;
    }

    /**
     * DTO로부터 카테고리 수정
     */
    public boolean updateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null || !categoryDTO.isValid() || categoryDTO.getId() == null) {
            return false;
        }

        // 기존 카테고리 정보 조회
        CategoryDTO existingCategory = categoryDAO.findById(categoryDTO.getId());
        if (existingCategory == null) {
            return false;
        }

        // 전체 카테고리명과 레벨 자동 설정
        categoryDTO = setFullCategoryNameAndLevel(categoryDTO);

        return categoryDAO.update(categoryDTO);
    }

    /**
     * 카테고리 삭제
     */
    public boolean deleteCategory(Long categoryId) {
        // 하위 카테고리가 있는지 확인
        List<CategoryDTO> subcategories = categoryDAO.findByParentId(categoryId);
        if (!subcategories.isEmpty()) {
            // 하위 카테고리가 있으면 모두 삭제 처리
            for (CategoryDTO subcategory : subcategories) {
                deleteCategory(subcategory.getId());
            }
        }

        return categoryDAO.delete(categoryId);
    }

    /**
     * 카테고리명으로 DTO 검색
     */
    public List<CategoryDTO> searchCategoryDTOs(String keyword) {
        return categoryDAO.searchByName(keyword);
    }

    /**
     * 카테고리 추가
     *
     * @param categoryDTO
     * @return 성공시 true, 실패시 false
     */
    public boolean createCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null || !categoryDTO.isValid()) {
            return false;
        }

        // 전체 카테고리명과 레벨 자동 설정
        categoryDTO = setFullCategoryNameAndLevel(categoryDTO);

        return categoryDAO.save(categoryDTO) > 0;
    }

    /**
     * 페이지네이션 처리된 카테고리 목록 조회
     */
    public List<CategoryDTO> getCategoryDTOsWithPagination(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return categoryDAO.findAllWithPagination(offset, pageSize);
    }

    /**
     * 카테고리명으로 카테고리 검색 (페이지네이션)
     */
    public List<CategoryDTO> searchCategoryDTOsWithPagination(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return categoryDAO.searchByNameWithPagination(keyword, offset, pageSize);
    }

    /**
     * 전체 카테고리 개수 조회
     */
    public int getTotalCategoryCount() {
        return categoryDAO.countAll();
    }

    /**
     * 검색 결과 카테고리 개수 조회
     */
    public int getSearchResultCount(String keyword) {
        return categoryDAO.countByName(keyword);
    }

    /**
     * PageDTO 생성 및 설정
     */
    public PageDTO setupCategoryPage(PageDTO pageDTO) {
        // 검색어가 있는 경우
        if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().trim().isEmpty()) {
            // 검색 결과 총 개수 조회
            pageDTO.setTotalCount(getSearchResultCount(pageDTO.getKeyword()));
        } else {
            // 전체 카테고리 개수 조회
            pageDTO.setTotalCount(getTotalCategoryCount());
        }

        // 페이지네이션 계산
        pageDTO.calculatePagination();

        return pageDTO;
    }

    /**
     * 요청 파라미터에서 PageDTO 생성
     */
    public PageDTO createPageDTOFromParameters(String pageParam, String keywordParam) {
        PageDTO pageDTO = new PageDTO();

        // 페이지 번호 설정
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                int currentPage = Integer.parseInt(pageParam);
                pageDTO.setCurrentPage(Math.max(1, currentPage));
            } catch (NumberFormatException e) {
                // 잘못된 형식이면 기본값 1 사용
                pageDTO.setCurrentPage(1);
            }
        }

        // 검색어 설정
        pageDTO.setKeyword(keywordParam);

        return pageDTO;
    }

    /**
     * PageDTO 기반으로 카테고리 목록 조회
     */
    public List<CategoryDTO> getCategoriesByPage(PageDTO pageDTO) {
        // 검색어가 있는 경우
        if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().trim().isEmpty()) {
            return searchCategoryDTOsWithPagination(
                    pageDTO.getKeyword(), pageDTO.getCurrentPage(), pageDTO.getPageSize());
        } else {
            return getCategoryDTOsWithPagination(
                    pageDTO.getCurrentPage(), pageDTO.getPageSize());
        }
    }
}