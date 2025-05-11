package domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private int currentPage = 1; // 현재 페이지 번호 (기본값 1)
    private int pageSize = 10;   // 페이지당 항목 수 (기본값 10)
    private int totalCount;      // 전체 항목 수
    private int totalPages;      // 전체 페이지 수
    private int startPage;       // 페이지네이션 시작 페이지
    private int endPage;         // 페이지네이션 끝 페이지
    private String sortBy;       // 정렬 옵션
    private String keyword;      // 검색어

    // 페이지네이션 계산 메서드
    public void calculatePagination() {
        // 전체 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 시작 페이지와 끝 페이지 계산
        this.startPage = Math.max(1, currentPage - 2);
        this.endPage = Math.min(totalPages, startPage + 4);
        this.startPage = Math.max(1, endPage - 4);
    }

    // 추가 유틸리티 메서드
    public int getOffset() {
        return (currentPage - 1) * pageSize;
    }

    public int getStartRow() {
        return getOffset() + 1;
    }

    public int getEndRow() {
        int endRow = currentPage * pageSize;
        return Math.min(endRow, totalCount);
    }

    // URL 파라미터 생성 메서드
    public String getPageParams() {
        StringBuilder params = new StringBuilder();
        params.append("page=").append(currentPage);

        if (sortBy != null && !sortBy.isEmpty()) {
            params.append("&sortBy=").append(sortBy);
        }

        if (keyword != null && !keyword.isEmpty()) {
            params.append("&keyword=").append(keyword);
        }

        return params.toString();
    }
}