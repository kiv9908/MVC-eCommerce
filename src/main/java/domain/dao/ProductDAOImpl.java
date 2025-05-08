package domain.dao;

import domain.model.Product;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    public ProductDAOImpl() {

    }

    @Override
    public Product findByProductCode(String productCode) {
        Product product = null;
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
                product = resultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return product;
    }

    @Override
    public List<Product> findByProductName(String productName) {
        List<Product> products = new ArrayList<>();
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
                Product product = resultSetToProduct(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return products;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
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
            closeResources(rs, pstmt, conn);
        }
        return products;
    }

    @Override
    public List<Product> findAllOrderByPrice(boolean ascending) {
        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
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
            closeResources(rs, pstmt, conn);
        }
        return products;
    }

    @Override
    public void save(Product product) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO TB_PRODUCT (no_product, nm_product, nm_detail_explain, id_file, " +
                    "dt_start_date, dt_end_date, qt_customer_price, qt_sale_price, qt_stock, qt_delivery_fee, " +
                    "no_register, da_first_date) VALUES ('PT' || LPAD(SEQ_TB_PRODUCT.NEXTVAL, 7, '0'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDetailExplain());
            // fileId가 null일 수 있으므로 적절히 처리
            if (product.getFileId() != null) {
                // 파일 ID 길이 체크 (데이터베이스 제약조건: 최대 30자)
            String fileId = product.getFileId();
            if (fileId != null && fileId.length() > 30) {
                // 30자로 잘라내기
                fileId = fileId.substring(0, 30);
                System.out.println("Warning: 파일 ID가 잘렸습니다. 원본: " + product.getFileId() + ", 저장된 값: " + fileId);
            }
            pstmt.setString(3, fileId);
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
            closeResources(null, pstmt, conn);
        }
    }

    @Override
    public void modify(Product product) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_PRODUCT SET nm_product = ?, nm_detail_explain = ?, id_file = ?, " +
                    "dt_start_date = ?, dt_end_date = ?, qt_customer_price = ?, qt_sale_price = ?, " +
                    "qt_stock = ?, qt_delivery_fee = ? WHERE no_product = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDetailExplain());
            // 파일 ID 길이 체크 (데이터베이스 제약조건: 최대 30자)
            String fileId = product.getFileId();
            if (fileId != null && fileId.length() > 30) {
                // 30자로 잘라내기
                fileId = fileId.substring(0, 30);
                System.out.println("Warning: 파일 ID가 잘렸습니다. 원본: " + product.getFileId() + ", 저장된 값: " + fileId);
            }
            pstmt.setString(3, fileId);
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
    
    /**
     * 상품 상태 계산 (재고에 따른 상태)
     */
    private String calculateStatus(Product product) {
        // 재고 확인
        if (product.getStock() != null && product.getStock() <= 0) {
            return "품절";
        }
        
        // 판매 기간에 따른 상태 확인
        String currentDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        
        if (product.getStartDate() != null && product.getEndDate() != null) {
            // 판매 종료일이 현재 날짜보다 과거인 경우 - 판매 중지
            if (product.getEndDate().compareTo(currentDate) < 0) {
                return "판매중지";
            }
        }
        
        // 위 조건 모두 해당하지 않으면 판매중
        return "판매중";
    }
    
    /**
     * 판매 기간에 따른 상태 계산
     */
    private String calculateStatusFromDates(String startDate, String endDate) {
        String currentDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        
        if (endDate != null && endDate.compareTo(currentDate) < 0) {
            return "판매중지";
        }
        
        return "판매중";
    }

    // closeResources 메서드 수정 - Connection 반환 추가
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