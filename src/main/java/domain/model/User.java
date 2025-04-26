package domain.model;

import java.util.Date;

public class User {
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

    public User() {
    }

    public User(String userId, String email, String password, String userName, String mobileNumber, String userType) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.mobileNumber = mobileNumber;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncPassword() {
        return encPassword;
    }

    public void setEncPassword(String encPassword) {
        this.encPassword = encPassword;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRegisterBy() {
        return registerBy;
    }

    public void setRegisterBy(String registerBy) {
        this.registerBy = registerBy;
    }

    public Date getFirstLoginDate() {
        return firstLoginDate;
    }

    public void setFirstLoginDate(Date firstLoginDate) {
        this.firstLoginDate = firstLoginDate;
    }
}

