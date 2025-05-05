package service;

import domain.dao.MappingDAO;
import domain.dto.MappingDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MappingService {
    
    private final MappingDAO mappingDAO;
    
    public MappingService(MappingDAO mappingDAO) {
        this.mappingDAO = mappingDAO;
    }
    
    /**
     * 모든 카테고리 매핑 목록을 가져옵니다.
     */
    public List<MappingDTO> getAllMappings() {
        try {
            return mappingDAO.getAllMappings();
        } catch (SQLException e) {
            log.error("전체 카테고리 매핑 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 상품 코드와 카테고리 ID로 특정 카테고리 매핑을 가져옵니다.
     */
    public MappingDTO getMappingByProductAndCategory(String productCode, Long categoryId) {
        try {
            return mappingDAO.getMappingByProductAndCategory(productCode, categoryId);
        } catch (SQLException e) {
            log.error("상품 코드 {}와 카테고리 ID {}로 매핑 조회 중 오류 발생: {}", productCode, categoryId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 새 카테고리 매핑을 생성합니다.
     */
    public boolean createMapping(MappingDTO mappingDTO) {
        try {
            return mappingDAO.createMapping(mappingDTO);
        } catch (SQLException e) {
            log.error("카테고리 매핑 생성 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 기존 카테고리 매핑을 업데이트합니다.
     */
    public boolean updateMapping(MappingDTO mappingDTO) {
        try {
            return mappingDAO.updateMapping(mappingDTO);
        } catch (SQLException e) {
            log.error("카테고리 매핑 업데이트 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    

    /**
     * 상품 코드와 카테고리 ID로 카테고리 매핑을 삭제합니다.
     */
    public boolean deleteMappingByProductAndCategory(String productCode, Long categoryId) {
        try {
            return mappingDAO.deleteMappingByProductAndCategory(productCode, categoryId);
        } catch (SQLException e) {
            log.error("상품 코드 {}와 카테고리 ID {}로 매핑 삭제 중 오류 발생: {}", productCode, categoryId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 키워드로 카테고리 매핑을 검색합니다.
     */
    public List<MappingDTO> searchMappings(String keyword) {
        try {
            return mappingDAO.searchMappings(keyword);
        } catch (SQLException e) {
            log.error("키워드 {}로 카테고리 매핑 검색 중 오류 발생: {}", keyword, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 모든 카테고리 정보를 가져옵니다.
     */
    public List<Map<String, Object>> getAllCategories() {
        try {
            return mappingDAO.getAllCategories();
        } catch (SQLException e) {
            log.error("전체 카테고리 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 모든 상품 정보를 가져옵니다.
     */
    public List<Map<String, Object>> getAllProducts() {
        try {
            return mappingDAO.getAllProducts();
        } catch (SQLException e) {
            log.error("전체 상품 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
