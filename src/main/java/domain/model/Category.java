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

public class Category {
    private int nbCategory;           // 카테고리 식별번호
    private Integer nbParentCategory; // 상위카테고리식별번호
    private String nmCategory;        // 카테고리명
    private String nmFullCategory;    // 전체 카테고리명
    private String nmExplain;         // 설명
    private Integer cnLevel;          // 레벨
    private Integer cnOrder;          // 순번
    private String ynUse;             // 사용유무
    private String ynDelete;          // 삭제여부
    private String noRegister;        // 등록자ID
    private Date daFirstDate;         // 등록일시


    @Override
    public String toString() {
        return "Category{" +
                "nbCategory=" + nbCategory +
                ", nbParentCategory=" + nbParentCategory +
                ", nmCategory='" + nmCategory + '\'' +
                ", nmFullCategory='" + nmFullCategory + '\'' +
                ", cnLevel=" + cnLevel +
                ", cnOrder=" + cnOrder +
                ", ynUse='" + ynUse + '\'' +
                '}';
    }
}