package service;

import domain.dao.CategoryDAO;
import domain.model.Category;

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
     * 모든 카테고리 조회
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
    
    /**
     * 활성화된 카테고리만 조회
     */
    public List<Category> getActiveCategories() {
        return categoryDAO.findAllActive();
    }
    
    /**
     * 카테고리 계층 구조 조회
     */
    public List<Category> getCategoryHierarchy() {
        return categoryDAO.findAllHierarchical();
    }
    
    /**
     * 카테고리 ID로 카테고리 조회
     */
    public Category getCategoryById(int categoryId) {
        return categoryDAO.findById(categoryId);
    }
    
    /**
     * 특정 레벨의 카테고리 조회
     */
    public List<Category> getCategoriesByLevel(int level) {
        return categoryDAO.findByLevel(level);
    }
    
    /**
     * 상위 카테고리 ID로 하위 카테고리 조회
     */
    public List<Category> getSubcategories(int parentCategoryId) {
        return categoryDAO.findByParentId(parentCategoryId);
    }
    
    /**
     * 카테고리 생성
     */
    public boolean createCategory(Category category) {
        // 새 카테고리의 ID 설정 (Oracle Sequence 사용)
        // DB Sequence를 직접 조회하여 할당하는 방식으로 변경 필요
        int nextCategoryId = getNextCategoryId();
        category.setNbCategory(nextCategoryId);
        
        // 상위 카테고리 지정되었을 경우 전체 카테고리명 설정
        if (category.getNbParentCategory() != null && category.getNbParentCategory() > 0) {
            Category parentCategory = categoryDAO.findById(category.getNbParentCategory());
            if (parentCategory != null) {
                // 부모 카테고리의 전체 경로 + 현재 카테고리명
                String fullCategoryName = parentCategory.getNmFullCategory() != null ? 
                        parentCategory.getNmFullCategory() + " > " + category.getNmCategory() : 
                        parentCategory.getNmCategory() + " > " + category.getNmCategory();
                category.setNmFullCategory(fullCategoryName);
                
                // 레벨 설정 (부모 레벨 + 1)
                if (parentCategory.getCnLevel() != null) {
                    category.setCnLevel(parentCategory.getCnLevel() + 1);
                } else {
                    // 기본적으로 부모가 있으면 최소 2레벨
                    category.setCnLevel(2);
                }
            }
        } else {
            // 최상위 카테고리인 경우
            category.setNmFullCategory(category.getNmCategory());
            category.setCnLevel(1); // 대분류는 1레벨
        }
        
        // 기본값 설정
        if (category.getYnUse() == null) {
            category.setYnUse("Y"); // 기본적으로 활성화
        }
        
        return categoryDAO.save(category) > 0;
    }
    
    /**
     * 카테고리 수정
     */
    public boolean updateCategory(Category category) {
        // 기존 카테고리 정보 조회
        Category existingCategory = categoryDAO.findById(category.getNbCategory());
        if (existingCategory == null) {
            return false;
        }
        
        // 상위 카테고리 변경 시 전체 카테고리명 갱신
        if (category.getNbParentCategory() != null && 
            (existingCategory.getNbParentCategory() == null || 
             !category.getNbParentCategory().equals(existingCategory.getNbParentCategory()))) {
            
            Category parentCategory = categoryDAO.findById(category.getNbParentCategory());
            if (parentCategory != null) {
                String fullCategoryName = parentCategory.getNmFullCategory() != null ? 
                        parentCategory.getNmFullCategory() + " > " + category.getNmCategory() : 
                        parentCategory.getNmCategory() + " > " + category.getNmCategory();
                category.setNmFullCategory(fullCategoryName);
                
                // 레벨 설정 (부모 레벨 + 1)
                if (parentCategory.getCnLevel() != null) {
                    category.setCnLevel(parentCategory.getCnLevel() + 1);
                }
            }
        } else if (category.getNbParentCategory() == null || category.getNbParentCategory() == 0) {
            // 최상위 카테고리로 변경된 경우
            category.setNmFullCategory(category.getNmCategory());
            category.setCnLevel(1);
        } else {
            // 카테고리명만 변경된 경우 전체 카테고리명 갱신
            if (!category.getNmCategory().equals(existingCategory.getNmCategory()) && 
                existingCategory.getNmFullCategory() != null) {
                
                String oldFullName = existingCategory.getNmFullCategory();
                String newFullName;
                
                if (existingCategory.getNbParentCategory() != null && existingCategory.getNbParentCategory() > 0) {
                    // 부모가 있는 경우
                    int lastSeparatorIndex = oldFullName.lastIndexOf(" > ");
                    if (lastSeparatorIndex != -1) {
                        newFullName = oldFullName.substring(0, lastSeparatorIndex) + " > " + category.getNmCategory();
                    } else {
                        newFullName = category.getNmCategory();
                    }
                } else {
                    // 최상위 카테고리인 경우
                    newFullName = category.getNmCategory();
                }
                
                category.setNmFullCategory(newFullName);
            }
        }
        
        return categoryDAO.update(category);
    }
    
    /**
     * 카테고리 삭제
     */
    public boolean deleteCategory(int categoryId) {
        // 하위 카테고리가 있는지 확인
        List<Category> subcategories = categoryDAO.findByParentId(categoryId);
        if (!subcategories.isEmpty()) {
            // 하위 카테고리가 있으면 모두 삭제 처리
            for (Category subcategory : subcategories) {
                deleteCategory(subcategory.getNbCategory());
            }
        }
        
        return categoryDAO.delete(categoryId);
    }
    
    /**
     * 카테고리 사용/비사용 설정
     */
    public boolean updateCategoryUseStatus(int categoryId, boolean isActive) {
        String status = isActive ? "Y" : "N";
        return categoryDAO.updateUseStatus(categoryId, status);
    }
    
    /**
     * 카테고리 순서 변경
     */
    public boolean updateCategoryOrder(int categoryId, int order) {
        return categoryDAO.updateOrder(categoryId, order);
    }
    
    /**
     * 카테고리명으로 검색
     */
    public List<Category> searchCategories(String keyword) {
        return categoryDAO.searchByName(keyword);
    }
    
    /**
     * 새 카테고리 ID 생성 (임시 구현)
     * 실제로는 Oracle Sequence를 사용하여 구현해야 함
     */
    private int getNextCategoryId() {
        // 임시 구현: 현재 존재하는 모든 카테고리 중 가장 큰 ID + 1
        List<Category> allCategories = categoryDAO.findAll();
        int maxId = 0;
        
        for (Category category : allCategories) {
            if (category.getNbCategory() > maxId) {
                maxId = category.getNbCategory();
            }
        }
        
        return maxId + 1;
    }
}