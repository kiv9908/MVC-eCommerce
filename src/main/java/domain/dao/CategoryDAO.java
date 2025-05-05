package domain.dao;

import domain.model.Category;

import java.util.List;

public interface CategoryDAO {
    
    // 모든 카테고리 조회
    List<Category> findAll();
    
    // 카테고리 식별번호로 카테고리 조회
    Category findById(int nbCategory);
    
    // 상위 카테고리로 하위 카테고리 목록 조회
    List<Category> findByParentId(int nbParentCategory);
    
    // 특정 레벨의 카테고리 목록 조회
    List<Category> findByLevel(int cnLevel);
    
    // 카테고리 저장
    int save(Category category);
    
    // 카테고리 수정
    boolean update(Category category);
    
    // 카테고리 삭제 (논리적 삭제 - YN_DELETE 필드 'Y'로 변경)
    boolean delete(int nbCategory);
    
    // 카테고리 사용/비사용 설정
    boolean updateUseStatus(int nbCategory, String ynUse);
    
    // 카테고리 정렬 순서 업데이트
    boolean updateOrder(int nbCategory, int cnOrder);
    
    // 카테고리명 검색
    List<Category> searchByName(String nmCategory);
    
    // 사용 중인 카테고리만 조회
    List<Category> findAllActive();
    
    // 전체 카테고리 계층 구조 조회 (대분류-중분류-소분류 형태로 조직화된 목록)
    List<Category> findAllHierarchical();
}