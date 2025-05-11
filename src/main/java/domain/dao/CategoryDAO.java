package domain.dao;

import domain.dto.CategoryDTO;

import java.util.List;

public interface CategoryDAO {
    
    // 모든 카테고리 조회
    List<CategoryDTO> findAll();
    
    // 카테고리 식별번호로 카테고리 조회
    CategoryDTO findById(Long nbCategory);
    
    // 상위 카테고리로 하위 카테고리 목록 조회
    List<CategoryDTO> findByParentId(Long nbParentCategory);

    // 카테고리 저장
    int save(CategoryDTO category);
    
    // 카테고리 수정
    boolean update(CategoryDTO category);
    
    // 카테고리 삭제 (논리적 삭제 - YN_DELETE 필드 'Y'로 변경)
    boolean delete(Long nbCategory);
    
    // 카테고리 사용/비사용 설정
    boolean updateUseStatus(Long nbCategory, String ynUse);
    
    // 카테고리 정렬 순서 업데이트
    boolean updateOrder(Long nbCategory, int cnOrder);
    
    // 카테고리명 검색
    List<CategoryDTO> searchByName(String nmCategory);

    // 페이지네이션 처리된 카테고리 목록 조회
    List<CategoryDTO> findAllWithPagination(int offset, int limit);

    // 검색 결과에 페이지네이션 적용
    List<CategoryDTO> searchByNameWithPagination(String nmCategory, int offset, int limit);

    // 전체 카테고리 개수 조회
    int countAll();

    // 검색 결과 카테고리 개수 조회
    int countByName(String nmCategory);
}