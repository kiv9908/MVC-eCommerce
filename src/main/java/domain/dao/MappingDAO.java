package domain.dao;

import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;

import java.sql.SQLException;
import java.util.List;

public interface MappingDAO {

    // 모든 매핑 가져오기
    List<MappingDTO> getAllMappings() throws SQLException;

    // 페이지네이션이 적용된 매핑 목록 가져오기
    List<MappingDTO> getMappingsWithPagination(PageDTO pageDTO) throws SQLException;

    // 전체 매핑 수 카운트
    int getTotalMappingCount() throws SQLException;

    // 키워드 검색 결과의 총 개수
    int getSearchMappingCount(String keyword) throws SQLException;

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

    // 키워드로 매핑 검색 + 페이지네이션
    List<MappingDTO> searchMappings(String keyword, PageDTO pageDTO) throws SQLException;

    // 모든 카테고리 정보 가져오기 (매핑 폼에서 사용)
    List<CategoryDTO> getAllCategories() throws SQLException;

    // 모든 상품 정보 가져오기 (매핑 폼에서 사용)
    List<ProductDTO> getAllProducts() throws SQLException;

    // 상품 코드로 매핑된 카테고리 목록 가져오기
    List<CategoryDTO> getMappingsByProductCode(String productCode) throws SQLException;

    // 상품 코드로 연결된 모든 매핑 삭제
    boolean deleteAllMappingsByProductCode(String productCode) throws SQLException;
}