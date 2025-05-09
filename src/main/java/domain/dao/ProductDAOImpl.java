package domain.dao;

import domain.dto.ProductDTO;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    public ProductDAOImpl() {

    }

    @Override
    public ProductDTO findByProductCode(String productCode) {
        ProductDTO productDTO = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_PRODUCT WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                productDTO = resultSetToProductDTO(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return productDTO;
    }

    @Override
    public List<ProductDTO> findByProductName(String productName) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_PRODUCT WHERE nm_product LIKE ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + productName + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProductDTO productDTO = resultSetToProductDTO(rs);
                products.add(productDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return products;
    }

    @Override
    public List<ProductDTO> findAll() {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_PRODUCT";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProductDTO productDTO = resultSetToProductDTO(rs);
                products.add(productDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return products;
    }

    @Override
    public List<ProductDTO> findAllOrderByPrice(boolean ascending) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_PRODUCT ORDER BY qt_sale_price " + (ascending ? "ASC" : "DESC");
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProductDTO productDTO = resultSetToProductDTO(rs);
                products.add(productDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return products;
    }

    @Override
    public void save(ProductDTO productDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO TB_PRODUCT (no_product, nm_product, nm_detail_explain, id_file, " +
                    "dt_start_date, dt_end_date, qt_customer_price, qt_sale_price, qt_stock, qt_delivery_fee, " +
                    "no_register, da_first_date) VALUES ('PT' || LPAD(SEQ_TB_PRODUCT.NEXTVAL, 7, '0'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productDTO.getProductName());
            pstmt.setString(2, productDTO.getDetailExplain());
            // fileId가 null일 수 있으므로 적절히 처리
            if (productDTO.getFileId() != null) {
                // 파일 ID 길이 체크 (데이터베이스 제약조건: 최대 30자)
            String fileId = productDTO.getFileId();
            if (fileId != null && fileId.length() > 30) {
                // 30자로 잘라내기
                fileId = fileId.substring(0, 30);
                System.out.println("Warning: 파일 ID가 잘렸습니다. 원본: " + productDTO.getFileId() + ", 저장된 값: " + fileId);
            }
            pstmt.setString(3, fileId);
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            pstmt.setString(4, productDTO.getStartDate());
            pstmt.setString(5, productDTO.getEndDate());

            if (productDTO.getCustomerPrice() != null) {
                pstmt.setInt(6, productDTO.getCustomerPrice());
            } else {
                pstmt.setNull(6, Types.NUMERIC);
            }

            pstmt.setInt(7, productDTO.getSalePrice());

            if (productDTO.getStock() != null) {
                pstmt.setInt(8, productDTO.getStock());
            } else {
                pstmt.setNull(8, Types.NUMERIC);
            }

            if (productDTO.getDeliveryFee() != null) {
                pstmt.setInt(9, productDTO.getDeliveryFee());
            } else {
                pstmt.setNull(9, Types.NUMERIC);
            }

            pstmt.setString(10, productDTO.getRegisterId());

            if (productDTO.getFirstDate() != null) {
                pstmt.setDate(11, new Date(productDTO.getFirstDate().getTime()));
            } else {
                pstmt.setNull(11, Types.DATE);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    @Override
    public void modify(ProductDTO productDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_PRODUCT SET nm_product = ?, nm_detail_explain = ?, id_file = ?, " +
                    "dt_start_date = ?, dt_end_date = ?, qt_customer_price = ?, qt_sale_price = ?, " +
                    "qt_stock = ?, qt_delivery_fee = ? WHERE no_product = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productDTO.getProductName());
            pstmt.setString(2, productDTO.getDetailExplain());
            // 파일 ID 길이 체크 (데이터베이스 제약조건: 최대 30자)
            String fileId = productDTO.getFileId();
            if (fileId != null && fileId.length() > 30) {
                // 30자로 잘라내기
                fileId = fileId.substring(0, 30);
                System.out.println("Warning: 파일 ID가 잘렸습니다. 원본: " + productDTO.getFileId() + ", 저장된 값: " + fileId);
            }
            pstmt.setString(3, fileId);
            pstmt.setString(4, productDTO.getStartDate());
            pstmt.setString(5, productDTO.getEndDate());

            if (productDTO.getCustomerPrice() != null) {
                pstmt.setInt(6, productDTO.getCustomerPrice());
            } else {
                pstmt.setNull(6, Types.NUMERIC);
            }

            pstmt.setInt(7, productDTO.getSalePrice());

            if (productDTO.getStock() != null) {
                pstmt.setInt(8, productDTO.getStock());
            } else {
                pstmt.setNull(8, Types.NUMERIC);
            }

            if (productDTO.getDeliveryFee() != null) {
                pstmt.setInt(9, productDTO.getDeliveryFee());
            } else {
                pstmt.setNull(9, Types.NUMERIC);
            }

            pstmt.setString(10, productDTO.getProductCode());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    @Override
    public boolean delete(String productCode) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "DELETE FROM TB_PRODUCT WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);

            int affectedRows = pstmt.executeUpdate();
            success = (affectedRows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    @Override
    public boolean modifyStock(String productCode, int stock) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_PRODUCT SET qt_stock = ? WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stock);
            pstmt.setString(2, productCode);

            System.out.println("===> 재고 변경 SQL: " + sql);
            System.out.println("===> 재고 변경 파라미터: stock=" + stock + ", productCode=" + productCode);

            int affectedRows = pstmt.executeUpdate();
            success = (affectedRows > 0);
            
            System.out.println("===> 재고 변경 결과: " + (success ? "성공" : "실패") + ", 영향받은 행: " + affectedRows);
        } catch (SQLException e) {
            System.out.println("===> 재고 변경 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    @Override
    public boolean modifySaleStatus(String productCode, String startDate, String endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
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
            closeResources(null, pstmt, conn);
        }

        return success;
    }


    private ProductDTO resultSetToProductDTO(ResultSet rs) throws SQLException {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductCode(rs.getString("NO_PRODUCT"));
        productDTO.setProductName(rs.getString("NM_PRODUCT"));
        productDTO.setDetailExplain(rs.getString("NM_DETAIL_EXPLAIN"));
        productDTO.setFileId(rs.getString("ID_FILE"));
        productDTO.setStartDate(rs.getString("DT_START_DATE"));
        productDTO.setEndDate(rs.getString("DT_END_DATE"));

        int customerPrice = rs.getInt("QT_CUSTOMER_PRICE");
        if (!rs.wasNull()) {
            productDTO.setCustomerPrice(customerPrice);
        }

        productDTO.setSalePrice(rs.getInt("QT_SALE_PRICE"));

        int stock = rs.getInt("QT_STOCK");
        if (!rs.wasNull()) {
            productDTO.setStock(stock);
        }

        int deliveryFee = rs.getInt("QT_DELIVERY_FEE");
        if (!rs.wasNull()) {
            productDTO.setDeliveryFee(deliveryFee);
        }

        productDTO.setRegisterId(rs.getString("NO_REGISTER"));
        productDTO.setFirstDate(rs.getDate("DA_FIRST_DATE"));

        return productDTO;
    }

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