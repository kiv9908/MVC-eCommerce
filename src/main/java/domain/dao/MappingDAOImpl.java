package domain.dao;

import domain.dto.CategoryDTO;
import domain.dto.MappingDTO;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MappingDAOImpl implements MappingDAO {

    public MappingDAOImpl() {}

    @Override
    public List<MappingDTO> getAllMappings() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<MappingDTO> mappingList = new ArrayList<>();
        String sql = "SELECT p.NO_PRODUCT, m.NB_CATEGORY, NM_PRODUCT, NM_FULL_CATEGORY, " +
                "m.CN_ORDER, m.NO_REGISTER, m.DA_FIRST_DATE " +
                "FROM tb_product p " +
                "JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                "JOIN tb_category c ON m.nb_category = c.nb_category " +
                "ORDER BY p.nm_product";

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                MappingDTO mapping = resultSetToMappingDTO(rs);
                mappingList.add(mapping);
            }
        } catch (SQLException e) {
            log.error("전체 매핑 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return mappingList;
    }

    @Override
    public List<MappingDTO> getMappingsWithPagination(PageDTO pageDTO) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MappingDTO> mappingList = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();

            // 정렬 조건 설정
            String orderBy = getSortColumn(pageDTO.getSortBy());

            String sql = "SELECT * FROM (" +
                    "    SELECT t.*, ROWNUM AS rnum FROM (" +
                    "        SELECT p.NO_PRODUCT, m.NB_CATEGORY, NM_PRODUCT, NM_FULL_CATEGORY, " +
                    "        m.CN_ORDER, m.NO_REGISTER, m.DA_FIRST_DATE " +
                    "        FROM tb_product p " +
                    "        JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                    "        JOIN tb_category c ON m.nb_category = c.nb_category " +
                    "        ORDER BY " + orderBy +
                    "    ) t" +
                    "    WHERE ROWNUM <= ?" +
                    ") WHERE rnum >= ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageDTO.getEndRow());
            pstmt.setInt(2, pageDTO.getStartRow());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                MappingDTO mapping = resultSetToMappingDTO(rs);
                mappingList.add(mapping);
            }
        } catch (SQLException e) {
            log.error("페이지네이션을 적용한 매핑 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return mappingList;
    }

    @Override
    public int getTotalMappingCount() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int totalCount = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM tb_category_product_mapping";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("전체 매핑 수 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return totalCount;
    }

    @Override
    public List<MappingDTO> searchMappings(String keyword, PageDTO pageDTO) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MappingDTO> mappingList = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();

            // 정렬 조건 설정
            String orderBy = getSortColumn(pageDTO.getSortBy());

            String sql = "SELECT * FROM (" +
                    "    SELECT t.*, ROWNUM AS rnum FROM (" +
                    "        SELECT p.NO_PRODUCT, m.NB_CATEGORY, NM_PRODUCT, NM_FULL_CATEGORY, " +
                    "        m.CN_ORDER, m.NO_REGISTER, m.DA_FIRST_DATE " +
                    "        FROM tb_product p " +
                    "        JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                    "        JOIN tb_category c ON m.nb_category = c.nb_category " +
                    "        WHERE p.NM_PRODUCT LIKE ? OR p.NO_PRODUCT LIKE ? OR c.NM_FULL_CATEGORY LIKE ? " +
                    "        ORDER BY " + orderBy +
                    "    ) t" +
                    "    WHERE ROWNUM <= ?" +
                    ") WHERE rnum >= ?";

            pstmt = conn.prepareStatement(sql);

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setInt(4, pageDTO.getEndRow());
            pstmt.setInt(5, pageDTO.getStartRow());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                MappingDTO mapping = resultSetToMappingDTO(rs);
                mappingList.add(mapping);
            }
        } catch (SQLException e) {
            log.error("키워드 검색 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return mappingList;
    }

    @Override
    public int getSearchMappingCount(String keyword) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) " +
                    "FROM tb_product p " +
                    "JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                    "JOIN tb_category c ON m.nb_category = c.nb_category " +
                    "WHERE p.NM_PRODUCT LIKE ? OR p.NO_PRODUCT LIKE ? OR c.NM_FULL_CATEGORY LIKE ?";

            pstmt = conn.prepareStatement(sql);

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("키워드로 매핑 수 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return count;
    }

    @Override
    public MappingDTO getMappingByProductAndCategory(String productCode, Long categoryId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        MappingDTO mapping = null;
        String sql = "SELECT p.NO_PRODUCT, m.NB_CATEGORY, NM_PRODUCT, NM_FULL_CATEGORY, " +
                "m.CN_ORDER, m.NO_REGISTER, m.DA_FIRST_DATE " +
                "FROM tb_product p " +
                "JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                "JOIN tb_category c ON m.nb_category = c.nb_category " +
                "WHERE p.NO_PRODUCT = ? AND m.NB_CATEGORY = ?";

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            pstmt.setLong(2, categoryId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                mapping = resultSetToMappingDTO(rs);
            }
        } catch (SQLException e) {
            log.error("매핑 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return mapping;
    }

    @Override
    public boolean createMapping(MappingDTO mappingDTO) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO tb_category_product_mapping (NO_PRODUCT, NB_CATEGORY, CN_ORDER, NO_REGISTER, DA_FIRST_DATE) VALUES (?, ?, ?, ?, SYSDATE)";
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, mappingDTO.getProductCode());
            pstmt.setLong(2, mappingDTO.getCategoryId());
            pstmt.setInt(3, mappingDTO.getDisplayOrder() != null ? mappingDTO.getDisplayOrder() : 1); // 기본값 1
            pstmt.setString(4, mappingDTO.getRegisterUser() != null ? mappingDTO.getRegisterUser() : "SYSTEM"); // 기본값 SYSTEM

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
        } catch (SQLException e) {
            log.error("매핑 생성 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    @Override
    public boolean updateMapping(MappingDTO mappingDTO) throws SQLException {
        Connection conn = null;
        PreparedStatement deleteStmt = null;
        PreparedStatement insertStmt = null;

        // 업데이트 시 기존 매핑 정보를 삭제하고 새 매핑 추가 (테이블 구조상 복합 키)
        String deleteSql = "DELETE FROM tb_category_product_mapping WHERE NO_PRODUCT = ? AND NB_CATEGORY = ?";
        String insertSql = "INSERT INTO tb_category_product_mapping (NO_PRODUCT, NB_CATEGORY, CN_ORDER, NO_REGISTER, DA_FIRST_DATE) VALUES (?, ?, ?, ?, SYSDATE)";

        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 기존 매핑 삭제 - 상품코드와 카테고리ID 모두 지정하여 정확한 매핑 삭제
            deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setString(1, mappingDTO.getProductCode());
            deleteStmt.setLong(2, mappingDTO.getCategoryId());
            deleteStmt.executeUpdate();

            // 새 매핑 추가
            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, mappingDTO.getProductCode());
            insertStmt.setLong(2, mappingDTO.getCategoryId());
            insertStmt.setInt(3, mappingDTO.getDisplayOrder() != null ? mappingDTO.getDisplayOrder() : 1); // 기본값 1
            insertStmt.setString(4, mappingDTO.getRegisterUser() != null ? mappingDTO.getRegisterUser() : "SYSTEM"); // 기본값 SYSTEM

            int affectedRows = insertStmt.executeUpdate();

            conn.commit(); // 트랜잭션 커밋
            success = affectedRows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // 오류 시 롤백
                } catch (SQLException ex) {
                    log.error("트랜잭션 롤백 중 오류 발생: {}", ex.getMessage(), ex);
                }
            }
            log.error("매핑 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (deleteStmt != null) {
                try {
                    deleteStmt.close();
                } catch (SQLException e) {
                    log.error("PreparedStatement 닫기 실패: {}", e.getMessage(), e);
                }
            }
            if (insertStmt != null) {
                try {
                    insertStmt.close();
                } catch (SQLException e) {
                    log.error("PreparedStatement 닫기 실패: {}", e.getMessage(), e);
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 원래 상태로 복원
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    log.error("Connection 닫기 실패: {}", e.getMessage(), e);
                }
            }
        }

        return success;
    }

    @Override
    public boolean deleteMappingByProductAndCategory(String productCode, Long categoryId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM tb_category_product_mapping WHERE NO_PRODUCT = ? AND NB_CATEGORY = ?";
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // 자동 커밋 끄기

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            pstmt.setLong(2, categoryId);

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;

            if (success) {
                conn.commit();  // 성공 시 커밋
            } else {
                conn.rollback();  // 실패 시 롤백
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();  // 예외 발생 시 롤백
            log.error("매핑 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    @Override
    public List<MappingDTO> searchMappings(String keyword) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<MappingDTO> mappingList = new ArrayList<>();
        String sql = "SELECT p.NO_PRODUCT, m.NB_CATEGORY, NM_PRODUCT, NM_FULL_CATEGORY, " +
                "m.CN_ORDER, m.NO_REGISTER, m.DA_FIRST_DATE " +
                "FROM tb_product p " +
                "JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                "JOIN tb_category c ON m.nb_category = c.nb_category " +
                "WHERE p.NM_PRODUCT LIKE ? OR p.NO_PRODUCT LIKE ? OR c.NM_FULL_CATEGORY LIKE ? " +
                "ORDER BY p.nm_product";

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                MappingDTO mapping = resultSetToMappingDTO(rs);
                mappingList.add(mapping);
            }
        } catch (SQLException e) {
            log.error("키워드 검색 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return mappingList;
    }

    @Override
    public List<CategoryDTO> getAllCategories() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT nb_category, nb_parent_category, nm_category, nm_full_category, " +
                "nm_explain, cn_level, cn_order, yn_use, yn_delete, " +
                "no_register, da_first_date " +
                "FROM tb_category WHERE YN_DELETE = 'N' ORDER BY nm_full_category";

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                CategoryDTO category = new CategoryDTO();
                category.setId(rs.getLong("nb_category"));
                category.setParentId(rs.getLong("nb_parent_category"));
                category.setName(rs.getString("nm_category"));
                category.setFullName(rs.getString("nm_full_category"));
                category.setDescription(rs.getString("nm_explain"));
                category.setLevel(rs.getInt("cn_level"));
                category.setOrder(rs.getInt("cn_order"));
                category.setUseYn(rs.getString("yn_use"));
                category.setDeleteYn(rs.getString("yn_delete"));
                category.setRegisterId(rs.getString("no_register"));
                category.setDaFirstDate(rs.getTimestamp("da_first_date"));

                categories.add(category);
            }
        } catch (SQLException e) {
            log.error("모든 카테고리 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return categories;
    }

    @Override
    public List<ProductDTO> getAllProducts() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ProductDTO> products = new ArrayList<>();
        String sql = "SELECT no_product, nm_product, da_first_date, no_register " +
                "FROM tb_product ORDER BY nm_product";

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProductDTO product = new ProductDTO();
                product.setProductCode(rs.getString("no_product"));
                product.setProductName(rs.getString("nm_product"));
                product.setFirstDate(rs.getTimestamp("da_first_date"));
                product.setRegisterId(rs.getString("no_register"));

                products.add(product);
            }
        } catch (SQLException e) {
            log.error("모든 상품 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return products;
    }

    @Override
    public List<CategoryDTO> getMappingsByProductCode(String productCode) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT c.nb_category, c.nb_parent_category, c.nm_category, c.nm_full_category, " +
                "c.nm_explain, c.cn_level, c.cn_order, c.yn_use, c.yn_delete, " +
                "c.no_register, c.da_first_date, m.cn_order AS mapping_order " +
                "FROM tb_category c " +
                "JOIN tb_category_product_mapping m ON c.nb_category = m.nb_category " +
                "WHERE m.NO_PRODUCT = ? AND c.yn_delete = 'N' " +
                "ORDER BY m.cn_order, c.nm_full_category";

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setId(rs.getLong("nb_category"));
                categoryDTO.setParentId(rs.getLong("nb_parent_category"));
                categoryDTO.setName(rs.getString("nm_category"));
                categoryDTO.setFullName(rs.getString("nm_full_category"));
                categoryDTO.setDescription(rs.getString("nm_explain"));
                categoryDTO.setLevel(rs.getInt("cn_level"));
                categoryDTO.setOrder(rs.getInt("cn_order"));
                categoryDTO.setUseYn(rs.getString("yn_use"));
                categoryDTO.setDeleteYn(rs.getString("yn_delete"));
                categoryDTO.setRegisterId(rs.getString("no_register"));
                categoryDTO.setDaFirstDate(rs.getTimestamp("da_first_date"));

                categories.add(categoryDTO);
            }
        } catch (SQLException e) {
            log.error("상품 코드로 카테고리 매핑 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return categories;
    }

    @Override
    public boolean deleteAllMappingsByProductCode(String productCode) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "DELETE FROM tb_category_product_mapping WHERE NO_PRODUCT = ?";
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // 자동 커밋 끄기

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);

            int affectedRows = pstmt.executeUpdate();
            // 삭제된 행이 없어도 성공으로 간주
            success = true;

            conn.commit();  // 성공 시 커밋
            log.info("상품 코드 {}로 {}개의 카테고리 매핑 삭제 완료", productCode, affectedRows);
        } catch (SQLException e) {
            if (conn != null) conn.rollback();  // 예외 발생 시 롤백
            log.error("상품 코드로 모든 카테고리 매핑 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    /**
     * ResultSet에서 MappingDTO 객체로 변환하는 유틸리티 메서드
     * @param rs ResultSet
     * @return MappingDTO 객체
     * @throws SQLException SQL 예외 발생 시
     */
    private MappingDTO resultSetToMappingDTO(ResultSet rs) throws SQLException {
        MappingDTO mapping = new MappingDTO();
        mapping.mapFromQueryResult(
                rs.getString("NO_PRODUCT"),
                rs.getLong("NB_CATEGORY"),
                rs.getString("NM_PRODUCT"),
                rs.getString("NM_FULL_CATEGORY"),
                rs.getInt("CN_ORDER"),
                rs.getString("NO_REGISTER"),
                rs.getDate("DA_FIRST_DATE")
        );

        // 테이블 표시용 ID 설정
        mapping.setId(rs.getLong("NB_CATEGORY"));

        return mapping;
    }

    /**
     * 정렬 컬럼을 가져오는 유틸리티 메서드
     * @param sortBy 정렬 기준 문자열
     * @return SQL 정렬 컬럼 문자열
     */
    private String getSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return "p.nm_product ASC";
        }

        switch (sortBy) {
            case "nameAsc":
                return "p.nm_product ASC";
            case "nameDesc":
                return "p.nm_product DESC";
            case "categoryAsc":
                return "c.nm_full_category ASC";
            case "categoryDesc":
                return "c.nm_full_category DESC";
            case "orderAsc":
                return "m.cn_order ASC";
            case "orderDesc":
                return "m.cn_order DESC";
            default:
                return "p.nm_product ASC";
        }
    }

    /**
     * 데이터베이스 리소스를 닫는 유틸리티 메서드
     * @param rs ResultSet
     * @param pstmt PreparedStatement
     * @param conn Connection
     */
    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            log.error("리소스 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}