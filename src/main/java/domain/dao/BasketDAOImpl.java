package domain.dao;

import domain.dto.BasketDTO;
import domain.dto.BasketItemDTO;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasketDAOImpl implements BasketDAO {

    @Override
    public BasketDTO findBasketByUserId(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM tb_basket WHERE id_user = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                BasketDTO basketDTO = new BasketDTO();
                basketDTO.setBasketId(rs.getLong("nb_basket"));
                basketDTO.setUserId(rs.getString("id_user"));
                basketDTO.setTotalAmount(rs.getInt("qt_basket_amount"));
                basketDTO.setRegisterId(rs.getString("no_register"));
                basketDTO.setCreatedDate(rs.getDate("da_first_date"));
                return basketDTO;
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean createBasket(BasketDTO basketDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "INSERT INTO tb_basket (nb_basket, id_user, qt_basket_amount, no_register, da_first_date) " +
                    "VALUES (SEQ_TB_BASKET.NEXTVAL, ?, ?, ?, SYSDATE)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, basketDTO.getUserId());
            pstmt.setInt(2, basketDTO.getTotalAmount() != null ? basketDTO.getTotalAmount() : 0);
            pstmt.setString(3, basketDTO.getRegisterId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean updateBasketAmount(Long basketId, int totalAmount) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE tb_basket SET qt_basket_amount = ? WHERE nb_basket = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, totalAmount);
            pstmt.setLong(2, basketId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<BasketItemDTO> findBasketItemsByBasketId(Long basketId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BasketItemDTO> basketItems = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();

            // 장바구니 항목과 상품 정보를 조인하여 가져오기
            String sql = "SELECT bi.*, p.nm_product, p.qt_customer_price, p.id_file, p.qt_stock, p.qt_delivery_fee " +
                    "FROM tb_basket_item bi " +
                    "LEFT JOIN tb_product p ON bi.no_product = p.no_product " +
                    "WHERE bi.nb_basket = ? " +
                    "ORDER BY bi.cn_basket_item_order";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, basketId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                BasketItemDTO item = new BasketItemDTO();
                item.setItemId(rs.getLong("nb_basket_item"));
                item.setBasketId(rs.getLong("nb_basket"));
                item.setItemOrder(rs.getInt("cn_basket_item_order"));
                item.setProductCode(rs.getString("no_product"));
                item.setUserId(rs.getString("id_user"));
                item.setPrice(rs.getInt("qt_basket_item_price"));
                item.setQuantity(rs.getInt("qt_basket_item"));
                item.setAmount(rs.getInt("qt_basket_item_amount"));
                item.setRegisterId(rs.getString("no_register"));
                item.setCreatedDate(rs.getDate("da_first_date"));

                // 상품 정보 설정 (LEFT JOIN이므로 상품이 없을 수 있음)
                if (rs.getString("nm_product") != null) {
                    item.setProductName(rs.getString("nm_product"));
                    
                    // null 체크
                    if (!rs.wasNull()) {
                        item.setCustomerPrice(rs.getInt("qt_customer_price"));
                    }
                    
                    item.setFileId(rs.getString("id_file"));
                    
                    if (!rs.wasNull()) {
                        item.setStock(rs.getInt("qt_stock"));
                    }
                    
                    if (!rs.wasNull()) {
                        item.setDeliveryFee(rs.getInt("qt_delivery_fee"));
                    }

                }

                basketItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return basketItems;
    }

    @Override
    public BasketItemDTO findBasketItemByProductCode(Long basketId, String productCode) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM tb_basket_item WHERE nb_basket = ? AND no_product = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, basketId);
            pstmt.setString(2, productCode);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                BasketItemDTO item = new BasketItemDTO();
                item.setItemId(rs.getLong("nb_basket_item"));
                item.setBasketId(rs.getLong("nb_basket"));
                item.setItemOrder(rs.getInt("cn_basket_item_order"));
                item.setProductCode(rs.getString("no_product"));
                item.setUserId(rs.getString("id_user"));
                item.setPrice(rs.getInt("qt_basket_item_price"));
                item.setQuantity(rs.getInt("qt_basket_item"));
                item.setAmount(rs.getInt("qt_basket_item_amount"));
                item.setRegisterId(rs.getString("no_register"));
                item.setCreatedDate(rs.getDate("da_first_date"));
                return item;
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean addBasketItem(BasketItemDTO basketItemDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            // 현재 장바구니의 최대 항목 순번 조회
            int maxOrder = getMaxItemOrder(conn, basketItemDTO.getBasketId());

            String sql = "INSERT INTO tb_basket_item (nb_basket_item, nb_basket, cn_basket_item_order, " +
                    "no_product, id_user, qt_basket_item_price, qt_basket_item, qt_basket_item_amount, " +
                    "no_register, da_first_date) " +
                    "VALUES (SEQ_TB_BASKET_ITEM.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, basketItemDTO.getBasketId());
            pstmt.setInt(2, maxOrder + 1); // 새 항목은 최대 순번 + 1
            pstmt.setString(3, basketItemDTO.getProductCode());
            pstmt.setString(4, basketItemDTO.getUserId());
            pstmt.setInt(5, basketItemDTO.getPrice());
            pstmt.setInt(6, basketItemDTO.getQuantity());
            pstmt.setInt(7, basketItemDTO.getAmount());
            pstmt.setString(8, basketItemDTO.getRegisterId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean updateBasketItemQuantity(Long itemId, int quantity, int amount) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE tb_basket_item SET qt_basket_item = ?, qt_basket_item_amount = ? WHERE nb_basket_item = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, amount);
            pstmt.setLong(3, itemId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 현재 장바구니의 최대 항목 순번 조회 (내부 사용)
    private int getMaxItemOrder(Connection conn, Long basketId) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT MAX(cn_basket_item_order) FROM tb_basket_item WHERE nb_basket = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, basketId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int maxOrder = rs.getInt(1);
                if (rs.wasNull()) {
                    return 0;
                }
                return maxOrder;
            }

            return 0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            // Connection은 닫지 않음 (외부에서 관리)
        }
    }

    @Override
    public boolean updateBasketItemPrice(Long itemId, int price, int amount) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE tb_basket_item SET qt_basket_item_price = ?, qt_basket_item_amount = ? WHERE nb_basket_item = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, price);
            pstmt.setInt(2, amount);
            pstmt.setLong(3, itemId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean updateBasketItemOrder(Long itemId, int order) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE tb_basket_item SET cn_basket_item_order = ? WHERE nb_basket_item = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, order);
            pstmt.setLong(2, itemId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean removeBasketItem(Long basketItemId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "DELETE FROM tb_basket_item WHERE nb_basket_item = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, basketItemId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean removeMultipleBasketItems(Long basketId, List<Long> basketItemIds) {
        if (basketItemIds == null || basketItemIds.isEmpty()) {
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            // 쿼리를 동적으로 생성 (IN 절)
            String placeholders = basketItemIds.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(","));

            String sql = "DELETE FROM tb_basket_item WHERE nb_basket = ? AND nb_basket_item IN (" + placeholders + ")";
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, basketId);

            int paramIndex = 2;
            for (Long itemId : basketItemIds) {
                pstmt.setLong(paramIndex++, itemId);
            }

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean clearBasket(Long basketId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "DELETE FROM tb_basket_item WHERE nb_basket = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, basketId);

            int result = pstmt.executeUpdate();

            // 장바구니 항목이 없어도 성공으로 간주
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.closeConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}