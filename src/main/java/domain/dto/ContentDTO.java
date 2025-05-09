package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private String fileId;          // id_file - VARCHAR2(64)
    private String originalFileName; // nm_org_file - VARCHAR2(200)
    private String savedFileName;    // nm_save_file - VARCHAR2(200)
    private String filePath;         // nm_file_path - VARCHAR2(200)
    private byte[] saveFile;         // bo_save_file - BLOB (선택적으로 사용)
    private String fileExtension;    // nm_file_ext - VARCHAR2(10)
    private String fileType;         // cd_file_type - VARCHAR2(10)
    private Date saveDate;           // da_save - DATE
    private int hitCount;            // cn_hit - NUMBER(5)
    private String serviceId;        // id_service - VARCHAR2(50)
    private String orgFileId;        // id_org_file - VARCHAR2(64)
    private String content;          // cn_content - CHAR(18)
    private String registerNo;       // no_register - VARCHAR2(30)
    private Date firstDate;          // da_first_date - DATE
}