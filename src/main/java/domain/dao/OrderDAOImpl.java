package domain.dao;

import util.DatabaseConnection;
import domain.dto.OrderDTO;
import domain.dto.OrderItemDTO;
import domain.dto.PageDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

/**
 * OrderDAO 인터페이스 구현 클래스
 */
@Slf4j
public class OrderDAOImpl implements OrderDAO {

    @Override
    public List<OrderDTO> getOrderList(PageDTO pageDTO) {
        List<OrderDTO> orderList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM (" +
                    "    SELECT t.*, ROWNUM AS rnum FROM (" +
                    "        SELECT o.*, " +
                    "        (SELECT COUNT(*) FROM tb_order_item i WHERE i.id_order = o.id_order) AS total_item_count " +
                    "        FROM tb_order o " +
                    "        ORDER BY o.da_order DESC" +
                    "    ) t" +
                    "    WHERE ROWNUM <= ?" +
                    ") WHERE rnum >= ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageDTO.getEndRow());
            pstmt.setInt(2, pageDTO.getStartRow());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderDTO order = resultSetToOrderDTO(rs);
                orderList.add(order);
            }

        } catch (SQLException e) {
            log.error("주문 목록 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return orderList;
    }

    @Override
    public int getTotalOrderCount() {
        int totalCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM tb_order";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalCount = rs.getInt(1);
            }

        } catch (SQLException e) {
            log.error("주문 총 개수 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return totalCount;
    }

    @Override
    public OrderDTO getOrderById(String orderId) {
        OrderDTO order = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM tb_order WHERE id_order = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                order = resultSetToOrderDTO(rs);
            }

        } catch (SQLException e) {
            log.error("주문 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return order;
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByOrderId(String orderId) {
        List<OrderItemDTO> itemList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT oi.*, p.nm_product, p.id_file " +
                    "FROM tb_order_item oi " +
                    "LEFT JOIN tb_product p ON oi.no_product = p.no_product " +
                    "WHERE oi.id_order = ? " +
                    "ORDER BY oi.cn_order_item ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItemDTO item = resultSetToOrderItemDTO(rs);
                itemList.add(item);
            }

        } catch (SQLException e) {
            log.error("주문 품목 목록 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return itemList;
    }

    @Override
    public OrderItemDTO getOrderItemById(String orderItemId) {
        OrderItemDTO item = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT oi.*, p.nm_product, p.id_file " +
                    "FROM tb_order_item oi " +
                    "LEFT JOIN tb_product p ON oi.no_product = p.no_product " +
                    "WHERE oi.id_order_item = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderItemId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                item = resultSetToOrderItemDTO(rs);
            }

        } catch (SQLException e) {
            log.error("주문 품목 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return item;
    }

    @Override
    public String createOrder(OrderDTO orderDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String orderId = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 먼저 새 주문 ID를 조회
            String getIdSql = "SELECT 'OD' || LPAD(seq_tb_order.nextval, 7, '0') AS id_order FROM DUAL";
            pstmt = conn.prepareStatement(getIdSql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                orderId = rs.getString("id_order");
            } else {
                throw new SQLException("주문 ID 생성 실패");
            }

            rs.close();
            pstmt.close();

            // 생성된 ID로 주문 삽입
            String insertSql = "INSERT INTO tb_order (id_order, id_user, qt_order_amount, qt_deli_money, " +
                    "qt_deli_period, nm_order_person, nm_receiver, no_delivery_zipno, nm_delivery_address, " +
                    "nm_receiver_telno, nm_delivery_space, cd_order_type, da_order, st_order, st_payment, " +
                    "no_register, da_first_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?, SYSDATE)";

            pstmt = conn.prepareStatement(insertSql);

            pstmt.setString(1, orderId);
            pstmt.setString(2, orderDTO.getUserId());
            pstmt.setInt(3, orderDTO.getOrderAmount() != null ? orderDTO.getOrderAmount() : 0);
            pstmt.setInt(4, orderDTO.getDeliveryFee() != null ? orderDTO.getDeliveryFee() : 0);
            pstmt.setInt(5, orderDTO.getDeliveryPeriod() != null ? orderDTO.getDeliveryPeriod() : 0);
            pstmt.setString(6, orderDTO.getOrderPersonName());
            pstmt.setString(7, orderDTO.getReceiverName());
            pstmt.setString(8, orderDTO.getDeliveryZipno());
            pstmt.setString(9, orderDTO.getDeliveryAddress());
            pstmt.setString(10, orderDTO.getReceiverTelno());
            pstmt.setString(11, orderDTO.getDeliverySpace());
            pstmt.setString(12, orderDTO.getOrderType() != null ? orderDTO.getOrderType() : "ORD1");
            pstmt.setString(13, orderDTO.getOrderStatus() != null ? orderDTO.getOrderStatus() : "ORD1");
            pstmt.setString(14, orderDTO.getPaymentStatus() != null ? orderDTO.getPaymentStatus() : "PAY1");
            pstmt.setString(15, orderDTO.getRegisterId());

            pstmt.executeUpdate();
            DatabaseConnection.commitTransaction(conn); // 트랜잭션 커밋

        } catch (SQLException e) {
            log.error("주문 생성 중 오류 발생: " + e.getMessage(), e);
            try {
                if (conn != null) {
                    DatabaseConnection.rollbackTransaction(conn); // 트랜잭션 롤백
                }
            } catch (SQLException ex) {
                log.error("트랜잭션 롤백 중 오류 발생: " + ex.getMessage(), ex);
            }
            return null;
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return orderId;
    }

    @Override
    public int createOrderItem(OrderItemDTO orderItemDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "INSERT INTO tb_order_item (id_order_item, id_order, cn_order_item, " +
                    "no_product, id_user, qt_unit_price, qt_order_item, qt_order_item_amount, " +
                    "qt_order_item_delivery_fee, st_payment, no_register, da_first_date) " +
                    "VALUES ('OT' || LPAD(seq_tb_order_item.nextval, 7, '0'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, orderItemDTO.getOrderId());
            pstmt.setInt(2, orderItemDTO.getOrderItemCount() != null ? orderItemDTO.getOrderItemCount() : 1);
            pstmt.setString(3, orderItemDTO.getProductCode());
            pstmt.setString(4, orderItemDTO.getUserId());
            pstmt.setInt(5, orderItemDTO.getUnitPrice() != null ? orderItemDTO.getUnitPrice() : 0);
            pstmt.setInt(6, orderItemDTO.getQuantity() != null ? orderItemDTO.getQuantity() : 1);
            pstmt.setInt(7, orderItemDTO.getAmount() != null ? orderItemDTO.getAmount() : 0);
            pstmt.setInt(8, orderItemDTO.getDeliveryFee() != null ? orderItemDTO.getDeliveryFee() : 0);
            pstmt.setString(9, orderItemDTO.getPaymentStatus() != null ? orderItemDTO.getPaymentStatus() : "PAY1");
            pstmt.setString(10, orderItemDTO.getRegisterId());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("주문 품목 생성 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public int updateOrderStatus(String orderId, String orderStatus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE tb_order SET st_order = ? WHERE id_order = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderStatus);
            pstmt.setString(2, orderId);

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("주문 상태 업데이트 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public int updatePaymentStatus(String orderId, String paymentStatus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE tb_order SET st_payment = ? WHERE id_order = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, paymentStatus);
            pstmt.setString(2, orderId);

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("결제 상태 업데이트 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public int updateOrder(OrderDTO orderDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE tb_order SET ");
            sql.append("qt_order_amount = ?, ");
            sql.append("qt_deli_money = ?, ");
            sql.append("qt_deli_period = ?, ");
            sql.append("nm_order_person = ?, ");
            sql.append("nm_receiver = ?, ");
            sql.append("no_delivery_zipno = ?, ");
            sql.append("nm_delivery_address = ?, ");
            sql.append("nm_receiver_telno = ?, ");
            sql.append("nm_delivery_space = ?, ");
            sql.append("cd_order_type = ?, ");
            sql.append("st_order = ?, ");
            sql.append("st_payment = ? ");
            sql.append("WHERE id_order = ?");

            pstmt = conn.prepareStatement(sql.toString());

            pstmt.setInt(1, orderDTO.getOrderAmount() != null ? orderDTO.getOrderAmount() : 0);
            pstmt.setInt(2, orderDTO.getDeliveryFee() != null ? orderDTO.getDeliveryFee() : 0);
            pstmt.setInt(3, orderDTO.getDeliveryPeriod() != null ? orderDTO.getDeliveryPeriod() : 0);
            pstmt.setString(4, orderDTO.getOrderPersonName());
            pstmt.setString(5, orderDTO.getReceiverName());
            pstmt.setString(6, orderDTO.getDeliveryZipno());
            pstmt.setString(7, orderDTO.getDeliveryAddress());
            pstmt.setString(8, orderDTO.getReceiverTelno());
            pstmt.setString(9, orderDTO.getDeliverySpace());
            pstmt.setString(10, orderDTO.getOrderType());
            pstmt.setString(11, orderDTO.getOrderStatus());
            pstmt.setString(12, orderDTO.getPaymentStatus());
            pstmt.setString(13, orderDTO.getOrderId());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("주문 업데이트 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public int updateOrderItem(OrderItemDTO orderItemDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE tb_order_item SET ");
            sql.append("qt_unit_price = ?, ");
            sql.append("qt_order_item = ?, ");
            sql.append("qt_order_item_amount = ?, ");
            sql.append("qt_order_item_delivery_fee = ?, ");
            sql.append("st_payment = ? ");
            sql.append("WHERE id_order_item = ?");

            pstmt = conn.prepareStatement(sql.toString());

            pstmt.setInt(1, orderItemDTO.getUnitPrice() != null ? orderItemDTO.getUnitPrice() : 0);
            pstmt.setInt(2, orderItemDTO.getQuantity() != null ? orderItemDTO.getQuantity() : 1);
            pstmt.setInt(3, orderItemDTO.getAmount() != null ? orderItemDTO.getAmount() : 0);
            pstmt.setInt(4, orderItemDTO.getDeliveryFee() != null ? orderItemDTO.getDeliveryFee() : 0);
            pstmt.setString(5, orderItemDTO.getPaymentStatus());
            pstmt.setString(6, orderItemDTO.getOrderItemId());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("주문 품목 업데이트 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public int deleteOrder(String orderId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 우선 주문에 속한 모든 주문 품목 삭제
            String deleteItemsSql = "DELETE FROM tb_order_item WHERE id_order = ?";
            pstmt = conn.prepareStatement(deleteItemsSql);
            pstmt.setString(1, orderId);
            pstmt.executeUpdate();

            // 그 다음 주문 삭제
            String deleteOrderSql = "DELETE FROM tb_order WHERE id_order = ?";
            pstmt.close();
            pstmt = conn.prepareStatement(deleteOrderSql);
            pstmt.setString(1, orderId);
            result = pstmt.executeUpdate();

            DatabaseConnection.commitTransaction(conn); // 트랜잭션 커밋

        } catch (SQLException e) {
            log.error("주문 삭제 중 오류 발생: " + e.getMessage(), e);
            try {
                if (conn != null) {
                    DatabaseConnection.rollbackTransaction(conn); // 트랜잭션 롤백
                }
            } catch (SQLException ex) {
                log.error("트랜잭션 롤백 중 오류 발생: " + ex.getMessage(), ex);
            }
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public int deleteOrderItem(String orderItemId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "DELETE FROM tb_order_item WHERE id_order_item = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderItemId);

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("주문 품목 삭제 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }

        return result;
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(String userId, PageDTO pageDTO) {
        List<OrderDTO> orderList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT * FROM (" +
                    "    SELECT t.*, ROWNUM AS rnum FROM (" +
                    "        SELECT o.*, " +
                    "        (SELECT COUNT(*) FROM tb_order_item i WHERE i.id_order = o.id_order) AS total_item_count " +
                    "        FROM tb_order o " +
                    "        WHERE o.id_user = ? " +
                    "        ORDER BY o.da_order DESC" +
                    "    ) t" +
                    "    WHERE ROWNUM <= ?" +
                    ") WHERE rnum >= ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, userId);
            pstmt.setInt(2, pageDTO.getEndRow());
            pstmt.setInt(3, pageDTO.getStartRow());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderDTO order = resultSetToOrderDTO(rs);
                orderList.add(order);
            }

        } catch (SQLException e) {
            log.error("사용자별 주문 목록 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return orderList;
    }

    @Override
    public int getTotalOrderCountByUserId(String userId) {
        int totalCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM tb_order WHERE id_user = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalCount = rs.getInt(1);
            }

        } catch (SQLException e) {
            log.error("사용자별 주문 총 개수 조회 중 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return totalCount;
    }

    /**
     * ResultSet에서 OrderDTO 객체로 변환하는 유틸리티 메서드
     * @param rs ResultSet
     * @return OrderDTO 객체
     * @throws SQLException SQL 예외 발생 시
     */
    private OrderDTO resultSetToOrderDTO(ResultSet rs) throws SQLException {
        OrderDTO order = new OrderDTO();
        order.setOrderId(rs.getString("id_order"));
        order.setUserId(rs.getString("id_user"));
        order.setOrderAmount(rs.getInt("qt_order_amount"));
        order.setDeliveryFee(rs.getInt("qt_deli_money"));
        order.setDeliveryPeriod(rs.getInt("qt_deli_period"));
        order.setOrderPersonName(rs.getString("nm_order_person"));
        order.setReceiverName(rs.getString("nm_receiver"));
        order.setDeliveryZipno(rs.getString("no_delivery_zipno"));
        order.setDeliveryAddress(rs.getString("nm_delivery_address"));
        order.setReceiverTelno(rs.getString("nm_receiver_telno"));
        order.setDeliverySpace(rs.getString("nm_delivery_space"));
        order.setOrderType(rs.getString("cd_order_type"));
        order.setOrderDate(rs.getTimestamp("da_order"));
        order.setOrderStatus(rs.getString("st_order"));
        order.setPaymentStatus(rs.getString("st_payment"));
        order.setRegisterId(rs.getString("no_register"));
        order.setFirstDate(rs.getTimestamp("da_first_date"));

        // total_item_count가 있는 경우에만 설정
        try {
            order.setTotalItemCount(rs.getInt("total_item_count"));
        } catch (SQLException e) {
            // 컬럼이 없으면 무시
        }

        return order;
    }

    /**
     * ResultSet에서 OrderItemDTO 객체로 변환하는 유틸리티 메서드
     * @param rs ResultSet
     * @return OrderItemDTO 객체
     * @throws SQLException SQL 예외 발생 시
     */
    private OrderItemDTO resultSetToOrderItemDTO(ResultSet rs) throws SQLException {
        OrderItemDTO item = new OrderItemDTO();
        item.setOrderItemId(rs.getString("id_order_item"));
        item.setOrderId(rs.getString("id_order"));
        item.setOrderItemCount(rs.getInt("cn_order_item"));
        item.setProductCode(rs.getString("no_product"));
        item.setUserId(rs.getString("id_user"));
        item.setUnitPrice(rs.getInt("qt_unit_price"));
        item.setQuantity(rs.getInt("qt_order_item"));
        item.setAmount(rs.getInt("qt_order_item_amount"));
        item.setDeliveryFee(rs.getInt("qt_order_item_delivery_fee"));
        item.setPaymentStatus(rs.getString("st_payment"));
        item.setRegisterId(rs.getString("no_register"));
        item.setFirstDate(rs.getTimestamp("da_first_date"));

        // 상품 정보가 있는 경우에만 설정
        try {
            item.setProductName(rs.getString("nm_product"));
            item.setFileId(rs.getString("id_file"));
        } catch (SQLException e) {
            // 컬럼이 없으면 무시
        }

        return item;
    }

    /**
     * 데이터베이스 리소스를 닫는 유틸리티 메서드
     * @param rs ResultSet
     * @param pstmt PreparedStatement
     * @param conn Connection
     */
    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        } catch (SQLException e) {
            log.error("리소스 해제 중 오류 발생: " + e.getMessage(), e);
        }
    }
}