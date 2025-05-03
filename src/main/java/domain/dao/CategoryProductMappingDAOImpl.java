package domain.dao;

import domain.model.Category;
import domain.model.CategoryProductMapping;
import domain.model.Product;
import lombok.extern.slf4j.Slf4j;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class CategoryProductMappingDAOImpl implements CategoryProductMappingDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    @Override
    public List<CategoryProductMapping> findByCategoryId(int nbCategory) {
        List<CategoryProductMapping> mappings = new ArrayList<>();
        String sql = "SELECT m.*, c.nm_category, p.nm_product " +
                     "FROM tb_category_product_mapping m " +
                     "JOIN tb_category c ON m.nb_category = c.nb_category " +
                     "JOIN tb_product p ON m.no_product = p.no_product " +
                     "WHERE m.nb_category = ? " +
                     "ORDER BY m.cn_order";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nbCategory);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CategoryProductMapping mapping = mapResultSetToMapping(rs);
                    
                    // 카테고리 정보 설정
                    Category category = new Category();
                    category.setNbCategory(rs.getInt("nb_category"));
                    category.setNmCategory(rs.getString("nm_category"));
                    mapping.setCategory(category);
                    
                    // 상품 정보 설정
                    Product product = new Product();
                    product.setProductCode(rs.getString("no_product"));
                    product.setProductName(rs.getString("nm_product"));
                    mapping.setProduct(product);
                    
                    mappings.add(mapping);
                }
            }
        } catch (SQLException e) {
            log.error("카테고리 ID로 매핑 조회 중 오류 발생: {}", nbCategory, e);
        }

        return mappings;
    }

    @Override
    public List<CategoryProductMapping> findByProductCode(String noProduct) {
        List<CategoryProductMapping> mappings = new ArrayList<>();
        String sql = "SELECT m.*, c.nm_category, p.nm_product " +
                     "FROM tb_category_product_mapping m " +
                     "JOIN tb_category c ON m.nb_category = c.nb_category " +
                     "JOIN tb_product p ON m.no_product = p.no_product " +
                     "WHERE m.no_product = ? " +
                     "ORDER BY c.cn_level, c.cn_order";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noProduct);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CategoryProductMapping mapping = mapResultSetToMapping(rs);
                    
                    // 카테고리 정보 설정
                    Category category = new Category();
                    category.setNbCategory(rs.getInt("nb_category"));
                    category.setNmCategory(rs.getString("nm_category"));
                    mapping.setCategory(category);
                    
                    // 상품 정보 설정
                    Product product = new Product();
                    product.setProductCode(rs.getString("no_product"));
                    product.setProductName(rs.getString("nm_product"));
                    mapping.setProduct(product);
                    
                    mappings.add(mapping);
                }
            }
        } catch (SQLException e) {
            log.error("상품 코드로 매핑 조회 중 오류 발생: {}", noProduct, e);
        }

        return mappings;
    }

    @Override
    public boolean save(CategoryProductMapping mapping) {
        String sql = "INSERT INTO tb_category_product_mapping (nb_category, no_product, cn_order, no_register, da_first_date) " +
                     "VALUES (?, ?, ?, ?, SYSDATE)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapping.getNbCategory());
            pstmt.setString(2, mapping.getNoProduct());
            
            if (mapping.getCnOrder() != null) {
                pstmt.setInt(3, mapping.getCnOrder());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, mapping.getNoRegister());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("매핑 저장 중 오류 발생", e);
        }

        return false;
    }

    @Override
    public boolean saveAll(List<CategoryProductMapping> mappings) {
        String sql = "INSERT INTO tb_category_product_mapping (nb_category, no_product, cn_order, no_register, da_first_date) " +
                     "VALUES (?, ?, ?, ?, SYSDATE)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (CategoryProductMapping mapping : mappings) {
                pstmt.setInt(1, mapping.getNbCategory());
                pstmt.setString(2, mapping.getNoProduct());
                
                if (mapping.getCnOrder() != null) {
                    pstmt.setInt(3, mapping.getCnOrder());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                
                pstmt.setString(4, mapping.getNoRegister());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();
            
            // 모든 항목이 성공적으로 추가되었는지 확인
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            
            return true;
        } catch (SQLException e) {
            log.error("매핑 일괄 저장 중 오류 발생", e);
        }

        return false;
    }

    @Override
    public boolean removeProductFromCategory(int nbCategory, String noProduct) {
        String sql = "DELETE FROM tb_category_product_mapping WHERE nb_category = ? AND no_product = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nbCategory);
            pstmt.setString(2, noProduct);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("카테고리에서 상품 제거 중 오류 발생", e);
        }

        return false;
    }

    @Override
    public boolean removeAllCategoriesForProduct(String noProduct) {
        String sql = "DELETE FROM tb_category_product_mapping WHERE no_product = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noProduct);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("상품의 모든 카테고리 연결 제거 중 오류 발생: {}", noProduct, e);
        }

        return false;
    }

    @Override
    public boolean removeAllProductsForCategory(int nbCategory) {
        String sql = "DELETE FROM tb_category_product_mapping WHERE nb_category = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nbCategory);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("카테고리의 모든 상품 연결 제거 중 오류 발생: {}", nbCategory, e);
        }

        return false;
    }

    @Override
    public boolean updateOrder(int nbCategory, String noProduct, int cnOrder) {
        String sql = "UPDATE tb_category_product_mapping SET cn_order = ? WHERE nb_category = ? AND no_product = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cnOrder);
            pstmt.setInt(2, nbCategory);
            pstmt.setString(3, noProduct);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("매핑 순서 업데이트 중 오류 발생", e);
        }

        return false;
    }

    @Override
    public List<Product> findProductsByCategoryId(int nbCategory) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* " +
                     "FROM tb_product p " +
                     "JOIN tb_category_product_mapping m ON p.no_product = m.no_product " +
                     "WHERE m.nb_category = ? " +
                     "ORDER BY m.cn_order";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nbCategory);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductCode(rs.getString("no_product"));
                    product.setProductName(rs.getString("nm_product"));
                    product.setDetailExplain(rs.getString("nm_detail_explain"));
                    product.setFileId(rs.getString("id_file"));
                    product.setStartDate(rs.getString("dt_start_date"));
                    product.setEndDate(rs.getString("dt_end_date"));
                    product.setCustomerPrice(rs.getInt("qt_customer_price"));
                    product.setSalePrice(rs.getInt("qt_sale_price"));
                    product.setStock(rs.getInt("qt_stock"));
                    product.setDeliveryFee(rs.getInt("qt_delivery_fee"));
                    product.setRegisterId(rs.getString("no_register"));
                    product.setFirstDate(rs.getTimestamp("da_first_date"));
                    
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            log.error("카테고리 ID로 상품 조회 중 오류 발생: {}", nbCategory, e);
        }

        return products;
    }

    @Override
    public int countProductsInCategory(int nbCategory) {
        String sql = "SELECT COUNT(*) FROM tb_category_product_mapping WHERE nb_category = ?";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nbCategory);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("카테고리의 상품 수 조회 중 오류 발생: {}", nbCategory, e);
        }

        return count;
    }

    @Override
    public boolean isProductInCategory(int nbCategory, String noProduct) {
        String sql = "SELECT COUNT(*) FROM tb_category_product_mapping WHERE nb_category = ? AND no_product = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nbCategory);
            pstmt.setString(2, noProduct);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error("상품의 카테고리 소속 확인 중 오류 발생", e);
        }

        return false;
    }
    
    // ResultSet에서 매핑 객체로 변환하는 메서드
    private CategoryProductMapping mapResultSetToMapping(ResultSet rs) throws SQLException {
        CategoryProductMapping mapping = new CategoryProductMapping();
        
        mapping.setNbCategory(rs.getInt("nb_category"));
        mapping.setNoProduct(rs.getString("no_product"));
        
        int cnOrder = rs.getInt("cn_order");
        if (!rs.wasNull()) {
            mapping.setCnOrder(cnOrder);
        }
        
        mapping.setNoRegister(rs.getString("no_register"));
        mapping.setDaFirstDate(rs.getTimestamp("da_first_date"));
        
        return mapping;
    }
}