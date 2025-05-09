package domain.dao;

import domain.dto.CategoryDTO;
import lombok.extern.slf4j.Slf4j;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CategoryDAOImpl implements CategoryDAO {
    
    
    @Override
    public List<CategoryDTO> findAll() {
        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE yn_delete = 'N' ORDER BY cn_order, nm_full_category";
        
        try (Connection conn = DatabaseConnection.getConnection();
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
    public CategoryDTO findById(int nbCategory) {
        String sql = "SELECT * FROM tb_category WHERE nb_category = ? AND yn_delete = 'N'";

        try (Connection conn = DatabaseConnection.getConnection();
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
    public List<CategoryDTO> findByParentId(int nbParentCategory) {
        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE nb_parent_category = ? AND yn_delete = 'N' ORDER BY cn_order";

        try (Connection conn = DatabaseConnection.getConnection();
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
    public int save(CategoryDTO categoryDTO) {
        String sql = "INSERT INTO tb_category (nb_category, nb_parent_category, nm_category, nm_full_category, nm_explain, cn_level, cn_order, yn_use, yn_delete, no_register, da_first_date) " +
                     "VALUES (SEQ_TB_CATEGORY.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'N', ?, SYSDATE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // nb_parent_category
            if (categoryDTO.getParentId() != null) {
                pstmt.setInt(1, categoryDTO.getParentId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            // nm_category
            pstmt.setString(2, categoryDTO.getName());

            // nm_full_category
            pstmt.setString(3, categoryDTO.getFullName());

            // nm_explain
            pstmt.setString(4, categoryDTO.getDescription());

            // cn_level
            if (categoryDTO.getLevel() != null) {
                pstmt.setInt(5, categoryDTO.getLevel());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            // cn_order
            if (categoryDTO.getOrder() != null) {
                pstmt.setInt(6, categoryDTO.getOrder());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            // yn_use
            pstmt.setString(7, categoryDTO.getUseYn());

            // no_register
            pstmt.setString(8, categoryDTO.getRegisterId());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("카테고리 저장 중 오류 발생", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(CategoryDTO categoryDTO) {
        String sql = "UPDATE tb_category SET nm_category = ?, nm_full_category = ?, nm_explain = ?, " +
                     "cn_level = ?, cn_order = ?, yn_use = ? WHERE nb_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryDTO.getName());
            pstmt.setString(2, categoryDTO.getFullName());
            pstmt.setString(3, categoryDTO.getDescription());

            if (categoryDTO.getLevel() != null) {
                pstmt.setInt(4, categoryDTO.getLevel());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            if (categoryDTO.getOrder() != null) {
                pstmt.setInt(5, categoryDTO.getOrder());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setString(6, categoryDTO.getUseYn());
            pstmt.setInt(7, categoryDTO.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("카테고리 수정 중 오류 발생: {}", categoryDTO.getId(), e);
        }

        return false;
    }

    @Override
    public boolean delete(int nbCategory) {
        String sql = "UPDATE tb_category SET yn_delete = 'Y' WHERE nb_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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

        try (Connection conn = DatabaseConnection.getConnection();
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

        try (Connection conn = DatabaseConnection.getConnection();
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
    public List<CategoryDTO> searchByName(String nmCategory) {
        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE nm_category LIKE ? AND yn_delete = 'N' ORDER BY cn_level, cn_order";

        try (Connection conn = DatabaseConnection.getConnection();
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

    
    // ResultSet에서 Category 객체로 매핑하는 유틸리티 메서드
    private CategoryDTO mapResultSetToCategory(ResultSet rs) throws SQLException {
        CategoryDTO categoryDTO = new CategoryDTO();
        
        categoryDTO.setId(rs.getInt("nb_category"));
        
        int parentCategory = rs.getInt("nb_parent_category");
        if (!rs.wasNull()) {
            categoryDTO.setParentId(parentCategory);
        }
        
        categoryDTO.setName(rs.getString("nm_category"));
        categoryDTO.setFullName(rs.getString("nm_full_category"));
        categoryDTO.setDescription(rs.getString("nm_explain"));
        
        int cnLevel = rs.getInt("cn_level");
        if (!rs.wasNull()) {
            categoryDTO.setLevel(cnLevel);
        }
        
        int cnOrder = rs.getInt("cn_order");
        if (!rs.wasNull()) {
            categoryDTO.setOrder(cnOrder);
        }
        
        categoryDTO.setUseYn(rs.getString("yn_use"));
        categoryDTO.setDeleteYn(rs.getString("yn_delete"));
        categoryDTO.setRegisterId(rs.getString("no_register"));
        categoryDTO.setDaFirstDate(rs.getTimestamp("da_first_date"));
        
        return categoryDTO;
    }
}