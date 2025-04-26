package domain.repository;

import domain.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {

    private Connection conn;

    // 데이터베이스 연결을 위한 생성자
    public ProductRepositoryImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Product findByProductCode(String productCode) {
        Product product = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM TB_PRODUCT WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                product = resultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt);
        }
        return product;
    }

    @Override
    public List<Product> findByProductName(String productName) {
        List<Product> products = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM TB_PRODUCT WHERE nm_product LIKE ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + productName + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = resultSetToProduct(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt);
        }
        return products;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM TB_PRODUCT";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = resultSetToProduct(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt);
        }
        return products;
    }

    @Override
    public List<Product> findAllOrderByPrice(boolean ascending) {
        List<Product> products = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM TB_PRODUCT ORDER BY qt_sale_price " + (ascending ? "ASC" : "DESC");
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = resultSetToProduct(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt);
        }
        return products;
    }

    @Override
    public void save(Product product) {
        PreparedStatement pstmt = null;

        try {
            String sql = "INSERT INTO TB_PRODUCT (no_product, nm_product, nm_detail_explain, id_file, " +
                    "dt_start_date, dt_end_date, qt_customer_price, qt_sale_price, qt_stock, qt_delivery_fee, " +
                    "no_register, da_first_date) VALUES ('P' || LPAD(PRODUCT_CODE_SEQ.NEXTVAL, 6, '0'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDetailExplain());
            // fileId가 null일 수 있으므로 적절히 처리
            if (product.getFileId() != null) {
                pstmt.setString(3, product.getFileId());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            pstmt.setString(4, product.getStartDate());
            pstmt.setString(5, product.getEndDate());

            if (product.getCustomerPrice() != null) {
                pstmt.setInt(6, product.getCustomerPrice());
            } else {
                pstmt.setNull(6, Types.NUMERIC);
            }

            pstmt.setInt(7, product.getSalePrice());

            if (product.getStock() != null) {
                pstmt.setInt(8, product.getStock());
            } else {
                pstmt.setNull(8, Types.NUMERIC);
            }

            if (product.getDeliveryFee() != null) {
                pstmt.setInt(9, product.getDeliveryFee());
            } else {
                pstmt.setNull(9, Types.NUMERIC);
            }

            pstmt.setString(10, product.getRegisterId());

            if (product.getFirstDate() != null) {
                pstmt.setDate(11, new Date(product.getFirstDate().getTime()));
            } else {
                pstmt.setNull(11, Types.DATE);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }
    }

    @Override
    public void update(Product product) {
        PreparedStatement pstmt = null;

        try {
            String sql = "UPDATE TB_PRODUCT SET nm_product = ?, nm_detail_explain = ?, id_file = ?, " +
                    "dt_start_date = ?, dt_end_date = ?, qt_customer_price = ?, qt_sale_price = ?, " +
                    "qt_stock = ?, qt_delivery_fee = ? WHERE no_product = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDetailExplain());
            pstmt.setString(3, product.getFileId());
            pstmt.setString(4, product.getStartDate());
            pstmt.setString(5, product.getEndDate());

            if (product.getCustomerPrice() != null) {
                pstmt.setInt(6, product.getCustomerPrice());
            } else {
                pstmt.setNull(6, Types.NUMERIC);
            }

            pstmt.setInt(7, product.getSalePrice());

            if (product.getStock() != null) {
                pstmt.setInt(8, product.getStock());
            } else {
                pstmt.setNull(8, Types.NUMERIC);
            }

            if (product.getDeliveryFee() != null) {
                pstmt.setInt(9, product.getDeliveryFee());
            } else {
                pstmt.setNull(9, Types.NUMERIC);
            }

            pstmt.setString(10, product.getProductCode());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }
    }

    @Override
    public boolean delete(String productCode) {
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            String sql = "DELETE FROM TB_PRODUCT WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);

            int affectedRows = pstmt.executeUpdate();
            success = (affectedRows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }

        return success;
    }

    @Override
    public boolean updateStock(String productCode, int stock) {
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            String sql = "UPDATE TB_PRODUCT SET qt_stock = ? WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stock);
            pstmt.setString(2, productCode);

            int affectedRows = pstmt.executeUpdate();
            success = (affectedRows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }

        return success;
    }

    @Override
    public boolean updateSaleStatus(String productCode, String startDate, String endDate) {
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            String sql = "UPDATE TB_PRODUCT SET dt_start_date = ?, dt_end_date = ? WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            pstmt.setString(3, productCode);

            int affectedRows = pstmt.executeUpdate();
            success = (affectedRows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }

        return success;
    }


    private Product resultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductCode(rs.getString("NO_PRODUCT"));
        product.setProductName(rs.getString("NM_PRODUCT"));
        product.setDetailExplain(rs.getString("NM_DETAIL_EXPLAIN"));
        product.setFileId(rs.getString("ID_FILE"));
        product.setStartDate(rs.getString("DT_START_DATE"));
        product.setEndDate(rs.getString("DT_END_DATE"));

        int customerPrice = rs.getInt("QT_CUSTOMER_PRICE");
        if (!rs.wasNull()) {
            product.setCustomerPrice(customerPrice);
        }

        product.setSalePrice(rs.getInt("QT_SALE_PRICE"));

        int stock = rs.getInt("QT_STOCK");
        if (!rs.wasNull()) {
            product.setStock(stock);
        }

        int deliveryFee = rs.getInt("QT_DELIVERY_FEE");
        if (!rs.wasNull()) {
            product.setDeliveryFee(deliveryFee);
        }

        product.setRegisterId(rs.getString("NO_REGISTER"));
        product.setFirstDate(rs.getDate("DA_FIRST_DATE"));

        return product;
    }

    private void closeResources(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}