package domain.dao;

import domain.dto.MappingDTO;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.DatabaseConnection.getConnection;

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
                
                // 테이블 표시용 ID 생성 (실제 ID가 없는 경우 productCode + categoryId 조합으로 유일한 값 생성)
                mapping.setId(rs.getLong("NB_CATEGORY"));  // 임시적으로 카테고리 ID를 사용
                
                mappingList.add(mapping);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        
        return mappingList;
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
                mapping = new MappingDTO();
                mapping.mapFromQueryResult(
                    rs.getString("NO_PRODUCT"), 
                    rs.getLong("NB_CATEGORY"), 
                    rs.getString("NM_PRODUCT"), 
                    rs.getString("NM_FULL_CATEGORY"),
                    rs.getInt("CN_ORDER"),
                    rs.getString("NO_REGISTER"),
                    rs.getDate("DA_FIRST_DATE")
                );
                // 복합키를 사용하므로 URL 식별을 위한 임시 ID 생성
                mapping.setId(categoryId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (deleteStmt != null) {
                try {
                    deleteStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (insertStmt != null) {
                try {
                    insertStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 원래 상태로 복원
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
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
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, productCode);
            pstmt.setLong(2, categoryId);
            
            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
                
                mappingList.add(mapping);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        
        return mappingList;
    }

    @Override
    public List<Map<String, Object>> getAllCategories() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT nb_category, nm_category, nm_full_category FROM tb_category ORDER BY nm_full_category";
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getLong("nb_category"));
                category.put("name", rs.getString("nm_category"));
                category.put("fullName", rs.getString("nm_full_category"));
                
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        
        return categories;
    }

    @Override
    public List<Map<String, Object>> getAllProducts() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = "SELECT no_product, nm_product FROM tb_product ORDER BY nm_product";
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("code", rs.getString("no_product"));
                product.put("name", rs.getString("nm_product"));
                
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        
        return products;
    }

    // closeResources 메서드 수정
    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) DatabaseConnection.closeConnection(conn); // 풀에 연결 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}