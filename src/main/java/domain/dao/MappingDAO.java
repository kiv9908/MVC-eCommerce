package domain.dao;

import domain.dto.MappingDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface MappingDAO {
    
    // 모든 매핑 가져오기
    List<MappingDTO> getAllMappings() throws SQLException;

    // 상품 코드와 카테고리 ID로 매핑 가져오기
    MappingDTO getMappingByProductAndCategory(String productCode, Long categoryId) throws SQLException;
    
    // 새 매핑 생성
    boolean createMapping(MappingDTO mappingDTO) throws SQLException;
    
    // 매핑 업데이트
    boolean updateMapping(MappingDTO mappingDTO) throws SQLException;


    // 매핑 삭제 (상품 코드와 카테고리 ID로)
    boolean deleteMappingByProductAndCategory(String productCode, Long categoryId) throws SQLException;
    
    // 키워드로 매핑 검색
    List<MappingDTO> searchMappings(String keyword) throws SQLException;
    
    // 모든 카테고리 정보 가져오기 (매핑 폼에서 사용)
    List<Map<String, Object>> getAllCategories() throws SQLException;
    
    // 모든 상품 정보 가져오기 (매핑 폼에서 사용)
    List<Map<String, Object>> getAllProducts() throws SQLException;
}
