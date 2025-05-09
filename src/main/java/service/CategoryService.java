package service;

import domain.dao.CategoryDAO;
import domain.dto.CategoryDTO;

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
    public CategoryDTO getCategoryDTOById(int categoryId) {
        return categoryDAO.findById(categoryId);
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
        // 상위 카테고리 변경 시 전체 카테고리명 갱신
        if (categoryDTO.getParentId() != null &&
                (existingCategory.getParentId() == null ||
                        !categoryDTO.getParentId().equals(existingCategory.getParentId()))) {

            CategoryDTO parentCategory = categoryDAO.findById(categoryDTO.getParentId());
            if (parentCategory != null) {
                String fullCategoryName = parentCategory.getFullName() != null ?
                        parentCategory.getFullName() + " > " + categoryDTO.getName() :
                        parentCategory.getName() + " > " + categoryDTO.getName();
                categoryDTO.setFullName(fullCategoryName);

                // 레벨 설정 (부모 레벨 + 1)
                if (parentCategory.getLevel() != null) {
                    categoryDTO.setLevel(parentCategory.getLevel() + 1);
                }
            }
        } else if (categoryDTO.getParentId() == null || categoryDTO.getParentId() == 0) {
            // 최상위 카테고리로 변경된 경우
            categoryDTO.setFullName(categoryDTO.getName());
            categoryDTO.setLevel(1);
        } else {
            // 카테고리명만 변경된 경우 전체 카테고리명 갱신
            if (!categoryDTO.getName().equals(existingCategory.getName()) &&
                    existingCategory.getFullName() != null) {

                String oldFullName = existingCategory.getFullName();
                String newFullName;

                if (existingCategory.getParentId() != null && existingCategory.getParentId() > 0) {
                    // 부모가 있는 경우
                    int lastSeparatorIndex = oldFullName.lastIndexOf(" > ");
                    if (lastSeparatorIndex != -1) {
                        newFullName = oldFullName.substring(0, lastSeparatorIndex) + " > " + categoryDTO.getName();
                    } else {
                        newFullName = categoryDTO.getName();
                    }
                } else {
                    // 최상위 카테고리인 경우
                    newFullName = categoryDTO.getName();
                }

                categoryDTO.setFullName(newFullName);
            }
        }

        return categoryDAO.update(categoryDTO);
    }


    /**
     * 카테고리 삭제
     */
    public boolean deleteCategory(int categoryId) {
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
        return categoryDAO.save(categoryDTO) > 0;
    }
}