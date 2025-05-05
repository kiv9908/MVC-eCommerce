package domain.dao;

import domain.model.Category;
import lombok.extern.slf4j.Slf4j;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
@Slf4j
public class CategoryDAOImpl implements CategoryDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE yn_delete = 'N' ORDER BY cn_level, cn_order";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            log.error("카테고리 전체 조회 중 오류 발생", e);
        }
        
        return categories;
    }
    
    @Override
    public Category findById(int nbCategory) {
        String sql = "SELECT * FROM tb_category WHERE nb_category = ? AND yn_delete = 'N'";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nbCategory);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            log.error("카테고리 ID 조회 중 오류 발생: " + nbCategory, e);
        }
        
        return null;
    }
    
    @Override
    public List<Category> findByParentId(int nbParentCategory) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE nb_parent_category = ? AND yn_delete = 'N' ORDER BY cn_order";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nbParentCategory);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            log.error("상위 카테고리로 하위 카테고리 조회 중 오류 발생: " + nbParentCategory, e);

        }
        
        return categories;
    }
    
    @Override
    public List<Category> findByLevel(int cnLevel) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE cn_level = ? AND yn_delete = 'N' ORDER BY cn_order";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cnLevel);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            log.error("레벨별 카테고리 조회 중 오류 발생: " + cnLevel, e);
        }
        
        return categories;
    }
    
    @Override
    public int save(Category category) {
        String sql = "INSERT INTO tb_category (nb_category, nb_parent_category, nm_category, nm_full_category, nm_explain, cn_level, cn_order, yn_use, yn_delete, no_register, da_first_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'N', ?, SYSDATE)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, category.getNbCategory());
            
            if (category.getNbParentCategory() != null) {
                pstmt.setInt(2, category.getNbParentCategory());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            pstmt.setString(3, category.getNmCategory());
            pstmt.setString(4, category.getNmFullCategory());
            pstmt.setString(5, category.getNmExplain());
            
            if (category.getCnLevel() != null) {
                pstmt.setInt(6, category.getCnLevel());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            if (category.getCnOrder() != null) {
                pstmt.setInt(7, category.getCnOrder());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            pstmt.setString(8, category.getYnUse());
            pstmt.setString(9, category.getNoRegister());
            
            return pstmt.executeUpdate();
            
        } catch (SQLException e) {
            log.error("카테고리 저장 중 오류 발생", e);

        }
        
        return 0;
    }
    
    @Override
    public boolean update(Category category) {
        String sql = "UPDATE tb_category SET nm_category = ?, nm_full_category = ?, nm_explain = ?, " +
                     "cn_level = ?, cn_order = ?, yn_use = ? WHERE nb_category = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getNmCategory());
            pstmt.setString(2, category.getNmFullCategory());
            pstmt.setString(3, category.getNmExplain());
            
            if (category.getCnLevel() != null) {
                pstmt.setInt(4, category.getCnLevel());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            if (category.getCnOrder() != null) {
                pstmt.setInt(5, category.getCnOrder());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setString(6, category.getYnUse());
            pstmt.setInt(7, category.getNbCategory());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            log.error("카테고리 수정 중 오류 발생: {}", category.getNbCategory(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean delete(int nbCategory) {
        String sql = "UPDATE tb_category SET yn_delete = 'Y' WHERE nb_category = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nbCategory);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            log.error("카테고리 삭제 중 오류 발생: {}", nbCategory, e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateUseStatus(int nbCategory, String ynUse) {
        String sql = "UPDATE tb_category SET yn_use = ? WHERE nb_category = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, ynUse);
            pstmt.setInt(2, nbCategory);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            log.error("카테고리 사용상태 변경 중 오류 발생: {}", nbCategory, e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateOrder(int nbCategory, int cnOrder) {
        String sql = "UPDATE tb_category SET cn_order = ? WHERE nb_category = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cnOrder);
            pstmt.setInt(2, nbCategory);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            log.error("카테고리 순서 변경 중 오류 발생: {}", nbCategory, e);
        }
        
        return false;
    }
    
    @Override
    public List<Category> searchByName(String nmCategory) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE nm_category LIKE ? AND yn_delete = 'N' ORDER BY cn_level, cn_order";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nmCategory + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            log.error("카테고리명 검색 중 오류 발생: {}", nmCategory, e);
        }
        
        return categories;
    }
    
    @Override
    public List<Category> findAllActive() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE yn_use = 'Y' AND yn_delete = 'N' ORDER BY cn_level, cn_order";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            log.error("활성화된 카테고리 조회 중 오류 발생", e);
        }
        
        return categories;
    }
    
    @Override
    public List<Category> findAllHierarchical() {
        // 전체 카테고리를 가져온 후 계층 구조로 정리
        List<Category> allCategories = findAll();
        List<Category> topLevelCategories = new ArrayList<>();
        Map<Integer, List<Category>> categoryMap = new HashMap<>();
        
        // 카테고리를 레벨별로 분류
        for (Category category : allCategories) {
            if (category.getNbParentCategory() == null || category.getNbParentCategory() == 0) {
                // 상위 카테고리가 없는 경우 (대분류)
                topLevelCategories.add(category);
            } else {
                // 상위 카테고리가 있는 경우 (중분류, 소분류)
                int parentId = category.getNbParentCategory();
                if (!categoryMap.containsKey(parentId)) {
                    categoryMap.put(parentId, new ArrayList<>());
                }
                categoryMap.get(parentId).add(category);
            }
        }
        
        // 대분류만 반환 (클라이언트에서 필요할 때 하위 카테고리를 요청하도록)
        return topLevelCategories;
    }
    
    // ResultSet에서 Category 객체로 매핑하는 유틸리티 메서드
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        
        category.setNbCategory(rs.getInt("nb_category"));
        
        int parentCategory = rs.getInt("nb_parent_category");
        if (!rs.wasNull()) {
            category.setNbParentCategory(parentCategory);
        }
        
        category.setNmCategory(rs.getString("nm_category"));
        category.setNmFullCategory(rs.getString("nm_full_category"));
        category.setNmExplain(rs.getString("nm_explain"));
        
        int cnLevel = rs.getInt("cn_level");
        if (!rs.wasNull()) {
            category.setCnLevel(cnLevel);
        }
        
        int cnOrder = rs.getInt("cn_order");
        if (!rs.wasNull()) {
            category.setCnOrder(cnOrder);
        }
        
        category.setYnUse(rs.getString("yn_use"));
        category.setYnDelete(rs.getString("yn_delete"));
        category.setNoRegister(rs.getString("no_register"));
        category.setDaFirstDate(rs.getTimestamp("da_first_date"));
        
        return category;
    }
}