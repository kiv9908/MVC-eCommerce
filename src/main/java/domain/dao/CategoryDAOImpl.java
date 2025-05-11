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
    public CategoryDTO findById(Long nbCategory) {
        String sql = "SELECT * FROM tb_category WHERE nb_category = ? AND yn_delete = 'N'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, nbCategory);

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
    public List<CategoryDTO> findByParentId(Long nbParentCategory) {
        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE nb_parent_category = ? AND yn_delete = 'N' ORDER BY cn_order";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, nbParentCategory);

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
        // 레벨 자동 계산 로직 추가
        if (categoryDTO.getLevel() == null) {
            // 상위 카테고리가 있는 경우
            if (categoryDTO.getParentId() != null) {
                // 상위 카테고리 정보 조회
                CategoryDTO parentCategory = findById(categoryDTO.getParentId());
                if (parentCategory != null) {
                    // 상위 카테고리 레벨이 있으면 +1, 없으면 1로 설정
                    int parentLevel = parentCategory.getLevel() != null ? parentCategory.getLevel() : 0;
                    categoryDTO.setLevel(parentLevel + 1);
                } else {
                    // 상위 카테고리가 없는 경우 0으로 설정 (대분류)
                    categoryDTO.setLevel(0);
                }
            } else {
                // 상위 카테고리가 없는 경우 0으로 설정 (대분류)
                categoryDTO.setLevel(0);
            }
        }

        String sql = "INSERT INTO tb_category (nb_category, nb_parent_category, nm_category, nm_full_category, nm_explain, cn_level, cn_order, yn_use, yn_delete, no_register, da_first_date) " +
                "VALUES (SEQ_TB_CATEGORY.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'N', ?, SYSDATE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // nb_parent_category
            if (categoryDTO.getParentId() != null) {
                pstmt.setLong(1, categoryDTO.getParentId());
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
        // 레벨 자동 계산 로직
        if (categoryDTO.getLevel() == null) {
            // 상위 카테고리가 있는 경우
            if (categoryDTO.getParentId() != null) {
                // 상위 카테고리 정보 조회
                CategoryDTO parentCategory = findById(categoryDTO.getParentId());
                if (parentCategory != null) {
                    // 상위 카테고리 레벨이 있으면 +1, 없으면 1로 설정
                    int parentLevel = parentCategory.getLevel() != null ? parentCategory.getLevel() : 0;
                    categoryDTO.setLevel(parentLevel + 1);
                } else {
                    // 상위 카테고리가 없는 경우 0으로 설정 (대분류)
                    categoryDTO.setLevel(0);
                }
            } else {
                // 상위 카테고리가 없는 경우 0으로 설정 (대분류)
                categoryDTO.setLevel(0);
            }
        }

        String sql = "UPDATE tb_category SET nm_category = ?, nm_full_category = ?, nm_explain = ?, " +
                "nb_parent_category = ?, cn_level = ?, cn_order = ?, yn_use = ? WHERE nb_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryDTO.getName());
            pstmt.setString(2, categoryDTO.getFullName());
            pstmt.setString(3, categoryDTO.getDescription());

            // nb_parent_category 설정 (인덱스 4)
            if (categoryDTO.getParentId() != null) {
                pstmt.setLong(4, categoryDTO.getParentId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            // 나머지 필드들의 인덱스도 1씩 증가
            if (categoryDTO.getLevel() != null) {
                pstmt.setInt(5, categoryDTO.getLevel());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            if (categoryDTO.getOrder() != null) {
                pstmt.setInt(6, categoryDTO.getOrder());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setString(7, categoryDTO.getUseYn());
            pstmt.setLong(8, categoryDTO.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("카테고리 수정 중 오류 발생: {}", categoryDTO.getId(), e);
        }

        return false;
    }

    @Override
    public boolean delete(Long nbCategory) {
        String sql = "UPDATE tb_category SET yn_delete = 'Y' WHERE nb_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, nbCategory);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("카테고리 삭제 중 오류 발생: {}", nbCategory, e);
        }

        return false;
    }

    @Override
    public boolean updateUseStatus(Long nbCategory, String ynUse) {
        String sql = "UPDATE tb_category SET yn_use = ? WHERE nb_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ynUse);
            pstmt.setLong(2, nbCategory);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("카테고리 사용상태 변경 중 오류 발생: {}", nbCategory, e);
        }

        return false;
    }

    @Override
    public boolean updateOrder(Long nbCategory, int cnOrder) {
        String sql = "UPDATE tb_category SET cn_order = ? WHERE nb_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cnOrder);
            pstmt.setLong(2, nbCategory);

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
        
        categoryDTO.setId(rs.getLong("nb_category"));

        Long parentCategory = rs.getLong("nb_parent_category");
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

    @Override
    public List<CategoryDTO> findAllWithPagination(int offset, int limit) {
        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE yn_delete = 'N' ORDER BY cn_order, nm_full_category OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            log.error("카테고리 페이지네이션 조회 중 오류 발생", e);
        }

        return categories;
    }

    @Override
    public List<CategoryDTO> searchByNameWithPagination(String nmCategory, int offset, int limit) {
        List<CategoryDTO> categories = new ArrayList<>();
        String sql = "SELECT * FROM tb_category WHERE nm_category LIKE ? AND yn_delete = 'N' ORDER BY cn_level, cn_order OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nmCategory + "%");
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            log.error("카테고리명 검색 페이지네이션 중 오류 발생: {}", nmCategory, e);
        }

        return categories;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM tb_category WHERE yn_delete = 'N'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("카테고리 개수 조회 중 오류 발생", e);
        }

        return 0;
    }

    @Override
    public int countByName(String nmCategory) {
        String sql = "SELECT COUNT(*) FROM tb_category WHERE nm_category LIKE ? AND yn_delete = 'N'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nmCategory + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("카테고리명 검색 개수 조회 중 오류 발생: {}", nmCategory, e);
        }

        return 0;
    }
}