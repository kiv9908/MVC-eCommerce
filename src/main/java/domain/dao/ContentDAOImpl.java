package domain.dao;

import domain.dto.ContentDTO;
import util.DatabaseConnection;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContentDAOImpl implements ContentDAO {
    
    @Override
    public ContentDTO findByFileId(String fileId) {
        ContentDTO contentDTO = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_CONTENT WHERE id_file = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fileId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                contentDTO = resultSetToContent(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return contentDTO;
    }
    
    @Override
    public List<ContentDTO> findByServiceId(String serviceId) {
        List<ContentDTO> contents = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM TB_CONTENT WHERE id_service = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, serviceId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ContentDTO contentDTO = resultSetToContent(rs);
                contents.add(contentDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return contents;
    }
    
    @Override
    public String save(ContentDTO contentDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String fileId = UUID.randomUUID().toString().replace("-", "");
        // ID_FILE 컬럼은 VARCHAR2(30)이므로 30자로 제한
        if (fileId.length() > 30) {
            fileId = fileId.substring(0, 30);
        }

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO TB_CONTENT (id_file, nm_org_file, nm_save_file, nm_file_path, " +
                    "bo_save_file, nm_file_ext, cd_file_type, da_save, cn_hit, id_service, id_org_file, " +
                    "cn_content, no_register, da_first_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, 0, ?, ?, ?, ?, SYSDATE)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fileId);
            pstmt.setString(2, contentDTO.getOriginalFileName());
            pstmt.setString(3, contentDTO.getSavedFileName());
            pstmt.setString(4, contentDTO.getFilePath());
            
            // BLOB 데이터 처리
            if (contentDTO.getSaveFile() != null) {
                pstmt.setBinaryStream(5, new ByteArrayInputStream(contentDTO.getSaveFile()), contentDTO.getSaveFile().length);
            } else {
                pstmt.setNull(5, Types.BLOB);
            }
            
            pstmt.setString(6, contentDTO.getFileExtension());
            pstmt.setString(7, contentDTO.getFileType());
            pstmt.setString(8, contentDTO.getServiceId());
            pstmt.setString(9, contentDTO.getOrgFileId());
            pstmt.setString(10, contentDTO.getContent());
            pstmt.setString(11, contentDTO.getRegisterNo());

            pstmt.executeUpdate();
            contentDTO.setFileId(fileId); // 생성된 ID를 객체에 설정
            return fileId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeResources(null, pstmt, conn);
        }
    }
    
    @Override
    public boolean update(ContentDTO contentDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
            // BLOB 데이터가 있는지 여부에 따라 다른 SQL 문 사용
            String sql;
            if (contentDTO.getSaveFile() != null) {
                sql = "UPDATE TB_CONTENT SET nm_org_file = ?, nm_save_file = ?, nm_file_path = ?, " +
                      "bo_save_file = ?, nm_file_ext = ?, cd_file_type = ?, id_service = ?, " +
                      "id_org_file = ?, cn_content = ? WHERE id_file = ?";
            } else {
                sql = "UPDATE TB_CONTENT SET nm_org_file = ?, nm_save_file = ?, nm_file_path = ?, " +
                      "nm_file_ext = ?, cd_file_type = ?, id_service = ?, " +
                      "id_org_file = ?, cn_content = ? WHERE id_file = ?";
            }
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, contentDTO.getOriginalFileName());
            pstmt.setString(2, contentDTO.getSavedFileName());
            pstmt.setString(3, contentDTO.getFilePath());
            
            int paramIndex = 4;
            if (contentDTO.getSaveFile() != null) {
                pstmt.setBinaryStream(paramIndex++, new ByteArrayInputStream(contentDTO.getSaveFile()), contentDTO.getSaveFile().length);
            }
            
            pstmt.setString(paramIndex++, contentDTO.getFileExtension());
            pstmt.setString(paramIndex++, contentDTO.getFileType());
            pstmt.setString(paramIndex++, contentDTO.getServiceId());
            pstmt.setString(paramIndex++, contentDTO.getOrgFileId());
            pstmt.setString(paramIndex++, contentDTO.getContent());
            pstmt.setString(paramIndex, contentDTO.getFileId());
            
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
    public boolean delete(String fileId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "DELETE FROM TB_CONTENT WHERE id_file = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fileId);
            
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
    public boolean incrementHitCount(String fileId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE TB_CONTENT SET cn_hit = cn_hit + 1 WHERE id_file = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fileId);
            
            int affectedRows = pstmt.executeUpdate();
            success = (affectedRows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }
        return success;
    }
    
    // ResultSet을 Content 객체로 변환하는 헬퍼 메서드
    private ContentDTO resultSetToContent(ResultSet rs) throws SQLException {
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setFileId(rs.getString("ID_FILE"));
        contentDTO.setOriginalFileName(rs.getString("NM_ORG_FILE"));
        contentDTO.setSavedFileName(rs.getString("NM_SAVE_FILE"));
        contentDTO.setFilePath(rs.getString("NM_FILE_PATH"));
        
        // BLOB 데이터 읽기 (필요한 경우)
        Blob blob = rs.getBlob("BO_SAVE_FILE");
        if (blob != null) {
            contentDTO.setSaveFile(blob.getBytes(1, (int) blob.length()));
        }
        
        contentDTO.setFileExtension(rs.getString("NM_FILE_EXT"));
        contentDTO.setFileType(rs.getString("CD_FILE_TYPE"));
        contentDTO.setSaveDate(rs.getDate("DA_SAVE"));
        contentDTO.setHitCount(rs.getInt("CN_HIT"));
        contentDTO.setServiceId(rs.getString("ID_SERVICE"));
        contentDTO.setOrgFileId(rs.getString("ID_ORG_FILE"));
        contentDTO.setContent(rs.getString("CN_CONTENT"));
        contentDTO.setRegisterNo(rs.getString("NO_REGISTER"));
        contentDTO.setFirstDate(rs.getDate("DA_FIRST_DATE"));
        
        return contentDTO;
    }
    
    // 리소스 정리 헬퍼 메서드
    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}