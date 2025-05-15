package domain.dao;

import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Slf4j
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
    public void save(ProductDTO productDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtGetId = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // 1. 먼저 시퀀스 값을 얻어와서 상품 코드 생성
            String getSeqSQL = "SELECT 'PT' || LPAD(SEQ_TB_PRODUCT.NEXTVAL, 7, '0') FROM DUAL";
            pstmtGetId = conn.prepareStatement(getSeqSQL);
            rs = pstmtGetId.executeQuery();

            String productCode = null;
            if (rs.next()) {
                productCode = rs.getString(1);
                productDTO.setProductCode(productCode); // DTO에 상품 코드 설정
                log.info("생성된 상품 코드: {}", productCode);
            } else {
                throw new SQLException("상품 코드 생성에 실패했습니다.");
            }

            // 2. 상품 테이블에 저장
            String sql = "INSERT INTO TB_PRODUCT (no_product, nm_product, nm_detail_explain, id_file, " +
                    "dt_start_date, dt_end_date, qt_customer_price, qt_sale_price, qt_stock, qt_delivery_fee, " +
                    "no_register, da_first_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);

            // 모든 파라미터 바인딩 명시적으로 처리
            pstmt.setString(1, productCode);
            pstmt.setString(2, productDTO.getProductName());

            // NULL 가능한 값 처리
            if (productDTO.getDetailExplain() != null) {
                pstmt.setString(3, productDTO.getDetailExplain());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            if (productDTO.getFileId() != null) {
                // 파일 ID 길이 체크 (데이터베이스 제약조건: 최대 30자)
                String fileId = productDTO.getFileId();
                if (fileId.length() > 30) {
                    fileId = fileId.substring(0, 30);
                    log.warn("파일 ID가 잘렸습니다. 원본: {}, 저장된 값: {}", productDTO.getFileId(), fileId);
                }
                pstmt.setString(4, fileId);
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            pstmt.setString(5, productDTO.getStartDate());
            pstmt.setString(6, productDTO.getEndDate());

            if (productDTO.getCustomerPrice() != null) {
                pstmt.setInt(7, productDTO.getCustomerPrice());
            } else {
                pstmt.setNull(7, Types.NUMERIC);
            }

            pstmt.setInt(8, productDTO.getSalePrice());

            if (productDTO.getStock() != null) {
                pstmt.setInt(9, productDTO.getStock());
            } else {
                pstmt.setNull(9, Types.NUMERIC);
            }

            if (productDTO.getDeliveryFee() != null) {
                pstmt.setInt(10, productDTO.getDeliveryFee());
            } else {
                pstmt.setNull(10, Types.NUMERIC);
            }

            pstmt.setString(11, productDTO.getRegisterId());

            if (productDTO.getFirstDate() != null) {
                pstmt.setDate(12, new Date(productDTO.getFirstDate().getTime()));
            } else {
                pstmt.setNull(12, Types.DATE);
            }

            pstmt.executeUpdate();
            log.info("상품 저장 성공: {}", productCode);

        } catch (SQLException e) {
            log.error("상품 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품 저장 실패", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtGetId != null) pstmtGetId.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                log.error("리소스 해제 중 오류 발생: {}", e.getMessage(), e);
            }
        }
    }

    // 상품 코드 생성 메소드
    private String generateProductCode(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT 'PT' || LPAD(SEQ_TB_PRODUCT.NEXTVAL, 7, '0') AS product_code FROM DUAL";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("product_code");
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
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

    @Override
    public List<ProductDTO> findAllWithPagination(int offset, int limit) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM TB_PRODUCT ORDER BY NO_PRODUCT OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);
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
    public List<ProductDTO> findAllOrderByPriceWithPagination(boolean ascending, int offset, int limit) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM TB_PRODUCT ORDER BY qt_sale_price " + (ascending ? "ASC" : "DESC") +
                    " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);
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
    public List<ProductDTO> findByProductNameWithPagination(String keyword, int offset, int limit) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM TB_PRODUCT WHERE nm_product LIKE ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
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
    public int countAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM TB_PRODUCT";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return count;
    }

    @Override
    public int countByProductName(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM TB_PRODUCT WHERE nm_product LIKE ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return count;
    }

    @Override
    public List<ProductDTO> findByProductNameOrderByPriceWithPagination(String keyword, boolean ascending, int offset, int limit) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM TB_PRODUCT WHERE nm_product LIKE ? " +
                    "ORDER BY qt_sale_price " + (ascending ? "ASC" : "DESC") +
                    " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
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
    public List<ProductDTO> findByCategoryId(Long categoryId, int offset, int limit) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT p.* FROM TB_PRODUCT p " +
                    "JOIN TB_CATEGORY_PRODUCT_MAPPING m ON p.no_product = m.no_product " +
                    "WHERE m.nb_category = ? " +
                    "ORDER BY p.da_first_date DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, categoryId);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
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
    public int countByCategoryId(Long categoryId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM TB_PRODUCT p " +
                    "JOIN TB_CATEGORY_PRODUCT_MAPPING m ON p.no_product = m.no_product " +
                    "WHERE m.nb_category = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return count;
    }

    @Override
    public List<ProductDTO> findByCategoryIdOrderByPriceWithPagination(Long categoryId, boolean ascending, int offset, int limit) {
        List<ProductDTO> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT p.* FROM TB_PRODUCT p " +
                    "JOIN TB_CATEGORY_PRODUCT_MAPPING m ON p.no_product = m.no_product " +
                    "WHERE m.nb_category = ? " +
                    "ORDER BY p.qt_sale_price " + (ascending ? "ASC" : "DESC") + " " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, categoryId);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
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

    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) DatabaseConnection.closeConnection(conn); // 풀에 연결 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getProductStock(String productCode) {
        int stock = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT qt_stock FROM tb_product WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productCode);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stock = rs.getInt("qt_stock");
            }
        } catch (SQLException e) {
            log.error("상품 재고 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return stock;
    }

    @Override
    public int updateProductStock(String productCode, int newStock) {
        int result = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "UPDATE tb_product SET qt_stock = ? WHERE no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newStock);
            pstmt.setString(2, productCode);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("상품 재고 업데이트 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }
}