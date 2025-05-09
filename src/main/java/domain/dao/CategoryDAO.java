package domain.dao;

import domain.dto.CategoryDTO;

import java.util.List;

public interface CategoryDAO {
    
    // 모든 카테고리 조회
    List<CategoryDTO> findAll();
    
    // 카테고리 식별번호로 카테고리 조회
    CategoryDTO findById(int nbCategory);
    
    // 상위 카테고리로 하위 카테고리 목록 조회
    List<CategoryDTO> findByParentId(int nbParentCategory);

    // 카테고리 저장
    int save(CategoryDTO category);
    
    // 카테고리 수정
    boolean update(CategoryDTO category);
    
    // 카테고리 삭제 (논리적 삭제 - YN_DELETE 필드 'Y'로 변경)
    boolean delete(int nbCategory);
    
    // 카테고리 사용/비사용 설정
    boolean updateUseStatus(int nbCategory, String ynUse);
    
    // 카테고리 정렬 순서 업데이트
    boolean updateOrder(int nbCategory, int cnOrder);
    
    // 카테고리명 검색
    List<CategoryDTO> searchByName(String nmCategory);

}