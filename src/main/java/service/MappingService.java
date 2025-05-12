package service;

import domain.dao.MappingDAO;
import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * 페이지네이션이 적용된 카테고리 매핑 목록을 가져옵니다.
     */
    public List<MappingDTO> getMappingsWithPagination(PageDTO pageDTO) {
        try {
            // 전체 매핑 수 조회
            int totalCount = mappingDAO.getTotalMappingCount();
            pageDTO.setTotalCount(totalCount);
            pageDTO.calculatePagination();

            // 페이지네이션 적용된 목록 조회
            return mappingDAO.getMappingsWithPagination(pageDTO);
        } catch (SQLException e) {
            log.error("페이지네이션을 적용한 카테고리 매핑 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 키워드로 검색된 매핑 목록을 페이지네이션과 함께 가져옵니다.
     */
    public List<MappingDTO> searchMappingsWithPagination(String keyword, PageDTO pageDTO) {
        try {
            // 검색 결과 수 조회
            int totalCount = mappingDAO.getSearchMappingCount(keyword);
            pageDTO.setTotalCount(totalCount);
            pageDTO.calculatePagination();

            // 페이지네이션 적용된 검색 결과 조회
            return mappingDAO.searchMappings(keyword, pageDTO);
        } catch (SQLException e) {
            log.error("페이지네이션을 적용한 키워드 검색 중 오류 발생: {}", e.getMessage(), e);
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
     * 매핑을 업데이트합니다.
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
     * 모든 카테고리 목록을 가져옵니다.
     */
    public List<CategoryDTO> getAllCategories() {
        try {
            return mappingDAO.getAllCategories();
        } catch (SQLException e) {
            log.error("모든 카테고리 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 모든 상품 목록을 가져옵니다.
     */
    public List<ProductDTO> getAllProducts() {
        try {
            return mappingDAO.getAllProducts();
        } catch (SQLException e) {
            log.error("모든 상품 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 상품 코드로 연결된 카테고리 목록을 가져옵니다.
     */
    public List<CategoryDTO> getMappingsByProductCode(String productCode) {
        try {
            return mappingDAO.getMappingsByProductCode(productCode);
        } catch (SQLException e) {
            log.error("상품 코드 {}로 카테고리 매핑 조회 중 오류 발생: {}", productCode, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 상품 코드로 모든 카테고리 매핑을 삭제합니다.
     */
    public boolean deleteAllMappingsByProductCode(String productCode) {
        try {
            return mappingDAO.deleteAllMappingsByProductCode(productCode);
        } catch (SQLException e) {
            log.error("상품 코드 {}로 모든 카테고리 매핑 삭제 중 오류 발생: {}", productCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 매핑의 총 개수를 조회합니다.
     */
    public int getTotalMappingCount() {
        try {
            return mappingDAO.getTotalMappingCount();
        } catch (SQLException e) {
            log.error("전체 매핑 수 조회 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 검색 조건에 맞는 매핑의 총 개수를 조회합니다.
     */
    public int getSearchMappingCount(String keyword) {
        try {
            return mappingDAO.getSearchMappingCount(keyword);
        } catch (SQLException e) {
            log.error("검색 매핑 수 조회 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }
}