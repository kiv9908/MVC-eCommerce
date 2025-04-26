package domain.repository;

import domain.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    private final Connection conn;

    // 데이터베이스 연결을 위한 생성자
    public UserRepositoryImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public User findByUserId(String email) {
        User user = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
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
            closeResources(rs, pstmt);
        }
        return user;
    }

    private void closeResources(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
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
        PreparedStatement pstmt = null;

        try {
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
            closeResources(null, pstmt);
        }
    }

    @Override
    public void update(User user) {
        PreparedStatement pstmt = null;

        try{
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
            closeResources(null, pstmt);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<User>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
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
            closeResources(rs, pstmt);
        }
        return users;
    }

    @Override
    public boolean deleteUser(String userId) {
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            // 실제 삭제 대신 상태값 변경(ST02: 탈퇴)
            String sql = "UPDATE TB_USER SET st_status = 'ST02' WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);

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
    public void updateUserRole(User user) {
        PreparedStatement pstmt = null;

        try {
            String sql = "UPDATE TB_USER SET cd_user_type = ? WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUserType());
            pstmt.setString(2, user.getEmail());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }
    }
}
