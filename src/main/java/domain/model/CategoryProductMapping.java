package domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductMapping {
    private int nbCategory;     // 카테고리 식별번호
    private String noProduct;   // 상품코드
    private Integer cnOrder;    // 순번
    private String noRegister;  // 등록자ID
    private Date daFirstDate;   // 등록일시
    
    // 조인된 객체 (실제 테이블에는 없음)
    private Category category;
    private Product product;

    public CategoryProductMapping(int categoryId, String productCode) {
        this.nbCategory = categoryId;
        this.noProduct = productCode;
    }


    @Override
    public String toString() {
        return "CategoryProductMapping{" +
                "nbCategory=" + nbCategory +
                ", noProduct='" + noProduct + '\'' +
                ", cnOrder=" + cnOrder +
                '}';
    }
}