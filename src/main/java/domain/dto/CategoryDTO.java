package domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import domain.model.Category;

/**
 * 카테고리 정보를 전송하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer id;
    private Integer parentId;
    private String name;
    private String fullName;
    private String description;
    private Integer level;
    private Integer order;
    private String useYn;
    private String deleteYn;
    private String registerId;
    
    /**
     * Category 엔티티로부터 DTO 객체 생성
     */
    public static CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getNbCategory());
        dto.setParentId(category.getNbParentCategory());
        dto.setName(category.getNmCategory());
        dto.setFullName(category.getNmFullCategory());
        dto.setDescription(category.getNmExplain());
        dto.setLevel(category.getCnLevel());
        dto.setOrder(category.getCnOrder());
        dto.setUseYn(category.getYnUse());
        dto.setDeleteYn(category.getYnDelete());
        dto.setRegisterId(category.getNoRegister());
        
        return dto;
    }
    
    /**
     * DTO 객체로부터 Category 엔티티 생성
     */
    public Category toEntity() {
        Category category = new Category();
        
        if (this.id != null) {
            category.setNbCategory(this.id);
        }
        
        category.setNbParentCategory(this.parentId);
        category.setNmCategory(this.name);
        category.setNmFullCategory(this.fullName);
        category.setNmExplain(this.description);
        category.setCnLevel(this.level);
        category.setCnOrder(this.order);
        category.setYnUse(this.useYn != null ? this.useYn : "Y");
        category.setYnDelete(this.deleteYn);
        category.setNoRegister(this.registerId);
        
        return category;
    }
    
    /**
     * 유효성 검증 메서드
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
}