package domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
public class MappingDTO {
    private Long id;
    private String productCode;
    private String productName;
    private Long categoryId;
    private String fullName;  // 전체 카테고리 경로 (예: 전자 > 컴퓨터 > 노트북)
    private Integer displayOrder; // 표시 순서 (cn_order)
    private String registerUser; // 등록자 (no_register)
    private Date registerDate; // 등록일 (da_first_date)
    
    // 쿼리에서 사용되는 필드
    private String noProduct; // 상품코드
    private Long nbCategory;  // 카테고리 ID
    private String nmProduct; // 상품명
    private String nmFullCategory; // 전체 카테고리 경로
    private Integer cnOrder; // 표시 순서
    private String noRegister; // 등록자
    private Date daFirstDate; // 등록일
    
    // 데이터베이스 값을 DTO 필드로 매핑
    public void mapFromQueryResult(String noProduct, Long nbCategory, String nmProduct, String nmFullCategory, 
                                  Integer cnOrder, String noRegister, Date daFirstDate) {
        this.noProduct = noProduct;
        this.productCode = noProduct;  // 매핑 리스트에서 사용되는 필드명 동기화
        
        this.nbCategory = nbCategory;
        this.categoryId = nbCategory;  // 매핑 리스트에서 사용되는 필드명 동기화
        
        this.nmProduct = nmProduct;
        this.productName = nmProduct;  // 매핑 리스트에서 사용되는 필드명 동기화
        
        this.nmFullCategory = nmFullCategory;
        this.fullName = nmFullCategory;  // 매핑 리스트에서 사용되는 필드명 동기화
        
        this.cnOrder = cnOrder;
        this.displayOrder = cnOrder;
        
        this.noRegister = noRegister;
        this.registerUser = noRegister;
        
        this.daFirstDate = daFirstDate;
        this.registerDate = daFirstDate;
    }
}
