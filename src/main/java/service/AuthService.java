package service;

import domain.dao.UserDAO;
import domain.dto.UserDTO;

public class AuthService {

    private final UserDAO userDAO;
    
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * 사용자 로그인 처리
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 인증된 사용자 객체
     * @throws Exception 인증 실패시 예외 발생
     */
    public UserDTO login(String email, String password) throws Exception {
        // 이메일로 사용자 조회
        UserDTO user = userDAO.findByUserId(email);
        
        // 사용자가 존재하지 않는 경우
        if (user == null) {
            throw new Exception("이메일에 해당하는 사용자를 찾을 수 없습니다: " + email);
        }
        
        // 탈퇴한 회원인 경우
        if ("ST02".equals(user.getStatus())) {
            throw new Exception("탈퇴한 회원입니다: " + email);
        }
        
        // 승인 대기 중인 회원인 경우
        if ("ST00".equals(user.getStatus())) {
            throw new Exception("아직 승인되지 않은 회원입니다. 관리자 승인 후 로그인이 가능합니다.");
        }
        
        // 비밀번호 검증
        if (!password.equals(user.getPassword())) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }
        
        return user;
    }
    
    /**
     * 현재 사용자가 관리자인지 확인
     * @param userDTO 확인할 사용자 객체
     * @return 관리자 여부
     */
    public boolean isAdmin(UserDTO userDTO) {
        return userDTO != null && "20".equals(userDTO.getUserType());
    }
}
