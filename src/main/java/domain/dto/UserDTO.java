package domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
public class UserDTO {
    private String userId;       // id_user, PK
    private String userName;     // nm_user
    private String password;     // nm_passwd (평문 비밀번호)
    private String encPassword;  // nm_enc_passwd (암호화된 비밀번호)
    private String mobileNumber; // no_mobile
    private String email;        // nm_email
    private String status;       // st_status (ST00, ST01, ST02)
    private String userType;     // cd_user_type (10: 일반사용자, 20: 관리자)
    private String registerBy;   // no_register
    private Date firstLoginDate; // da_first_date


    public UserDTO(String userId, String email, String password, String userName, String mobileNumber, String userType) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.mobileNumber = mobileNumber;
        this.userType = userType;
    }

}

