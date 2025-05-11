package domain.dao;

import domain.dto.UserDTO;
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
    }

    @Override
    public UserDTO findByUserId(String email) {
        UserDTO userDTO = null;
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
                userDTO = resultSetToUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 리소스 해제 및 연결 반환
            closeResources(rs, pstmt, conn);
        }
        return userDTO;
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

    private UserDTO resultSetToUser(ResultSet rs) throws SQLException {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(rs.getString("ID_USER"));
        userDTO.setUserName(rs.getString("NM_USER"));
        userDTO.setPassword(rs.getString("NM_PASWD"));
        userDTO.setEncPassword(rs.getString("NM_ENC_PASWD"));
        userDTO.setMobileNumber(rs.getString("NO_MOBILE"));
        userDTO.setEmail(rs.getString("NM_EMAIL"));
        userDTO.setStatus(rs.getString("ST_STATUS"));
        userDTO.setUserType(rs.getString("CD_USER_TYPE"));
        userDTO.setRegisterBy(rs.getString("NO_REGISTER"));
        userDTO.setFirstLoginDate(rs.getDate("DA_FIRST_DATE"));
        return userDTO;
    }


    @Override
    public void save(UserDTO userDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 커넥션 풀에서 연결 얻기
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO TB_USER (id_user, nm_user, nm_paswd, nm_enc_paswd, no_mobile, nm_email, st_status, cd_user_type, no_register, da_first_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userDTO.getUserId());
            pstmt.setString(2, userDTO.getUserName());
            pstmt.setString(3, userDTO.getPassword());
            pstmt.setString(4, userDTO.getEncPassword() != null ? userDTO.getEncPassword() : ""); // 암호화된 비밀번호가 null인 경우 빈 문자열로 처리
            pstmt.setString(5, userDTO.getMobileNumber());
            pstmt.setString(6, userDTO.getEmail());
            pstmt.setString(7, userDTO.getStatus());
            pstmt.setString(8, userDTO.getUserType());
            pstmt.setString(9, userDTO.getRegisterBy());

            if(userDTO.getFirstLoginDate() != null){
                pstmt.setDate(10, new java.sql.Date(userDTO.getFirstLoginDate().getTime()));
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
    public void modify(UserDTO userDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = DatabaseConnection.getConnection();

            String sql = "UPDATE TB_USER SET nm_user = ?, no_mobile = ?, st_status = ?, NM_PASWD = ? WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userDTO.getUserName());
            pstmt.setString(2, userDTO.getMobileNumber());
            pstmt.setString(3, userDTO.getStatus());
            pstmt.setString(4, userDTO.getPassword());
            pstmt.setString(5, userDTO.getEmail());

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
    public List<UserDTO> findAll() {
        List<UserDTO> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_USER";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while(rs.next()){
                UserDTO userDTO = resultSetToUser(rs);
                users.add(userDTO);
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
    public void modifyUserRole(UserDTO userDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_USER SET cd_user_type = ? WHERE nm_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userDTO.getUserType());
            pstmt.setString(2, userDTO.getEmail());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    @Override
    public List<UserDTO> findAllWithPagination(int offset, int limit) {
        List<UserDTO> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Oracle 페이지네이션 쿼리 사용
            String sql = "SELECT * FROM ("
                    + "SELECT tb.*, ROWNUM rnum FROM ("
                    + "SELECT * FROM TB_USER ORDER BY da_first_date DESC"
                    + ") tb WHERE ROWNUM <= ?"
                    + ") WHERE rnum > ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, offset + limit); // ROWNUM <= (offset + limit)
            pstmt.setInt(2, offset);         // rnum > offset

            rs = pstmt.executeQuery();

            while (rs.next()) {
                UserDTO userDTO = resultSetToUser(rs);
                users.add(userDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return users;
    }

    @Override
    public int countAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT COUNT(*) FROM TB_USER";
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
}