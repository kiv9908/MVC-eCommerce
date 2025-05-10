package domain.dto;

import lombok.*;

import java.sql.Timestamp;

/**
 * 카테고리 정보를 전송하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private Long parentId;
    private String name;
    private String fullName;
    private String description;
    private Integer level;
    private Integer order;
    private String useYn;
    private String deleteYn;
    private String registerId;
    private Timestamp daFirstDate;

    /**
     * 유효성 검증 메서드
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
}