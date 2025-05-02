package domain.dao;

import domain.model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    // Connection 필드 제거 및 생성자 수정
    public UserDAOImpl() {
        // 빈 생성자
    }

    @Override
    public User findByUserId(String email) {
        User user = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            // 커넥션 풀에서 연결 얻기
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_USER WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if(rs.next()){
                user = resultSetToUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 리소스 해제 및 연결 반환
            closeResources(rs, pstmt, conn);
        }
        return user;
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

    private User resultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("ID_USER"));
        user.setUserName(rs.getString("NM_USER"));
        user.setPassword(rs.getString("NM_PASWD"));
        user.setEncPassword(rs.getString("NM_ENC_PASWD"));
        user.setMobileNumber(rs.getString("NO_MOBILE"));
        user.setEmail(rs.getString("NM_EMAIL"));
        user.setStatus(rs.getString("ST_STATUS"));
        user.setUserType(rs.getString("CD_USER_TYPE"));
        user.setRegisterBy(rs.getString("NO_REGISTER"));
        user.setFirstLoginDate(rs.getDate("DA_FIRST_DATE"));
        return user;
    }


    @Override
    public void save(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 커넥션 풀에서 연결 얻기
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO TB_USER (id_user, nm_user, nm_paswd, nm_enc_paswd, no_mobile, nm_email, st_status, cd_user_type, no_register, da_first_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUserName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getEncPassword() != null ? user.getEncPassword() : ""); // 암호화된 비밀번호가 null인 경우 빈 문자열로 처리
            pstmt.setString(5, user.getMobileNumber());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getStatus());
            pstmt.setString(8, user.getUserType());
            pstmt.setString(9, user.getRegisterBy());

            if(user.getFirstLoginDate() != null){
                pstmt.setDate(10, new java.sql.Date(user.getFirstLoginDate().getTime()));
            }else {
                pstmt.setDate(10, null);
            }

            int affected = pstmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("사용자 등록에 실패했습니다. 영향 받은 행: " + affected);
            }

        } catch (SQLException e) {
            throw new RuntimeException("사용자 저장 중 데이터베이스 오류 발생: " + e.getMessage(), e);
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    @Override
    public void modify(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_USER SET nm_user = ? , no_mobile = ?, st_status = ? WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getMobileNumber());
            pstmt.setString(3, user.getStatus());
            pstmt.setString(4, user.getEmail());

            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeResources(null, pstmt, conn);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_USER";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while(rs.next()){
                User user = resultSetToUser(rs);
                users.add(user);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            closeResources(rs, pstmt, conn);
        }
        return users;
    }

    @Override
    public boolean deleteUser(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
            // 실제 삭제 대신 상태값 변경(ST03: 일시정지)
            String sql = "UPDATE TB_USER SET st_status = 'ST03' WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);

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
    public void modifyUserRole(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_USER SET cd_user_type = ? WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUserType());
            pstmt.setString(2, user.getEmail());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }
    }
}