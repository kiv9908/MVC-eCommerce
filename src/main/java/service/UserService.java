package service;

import domain.dao.UserDAO;
import domain.dto.UserDTO;
import exception.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
@Slf4j
public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void register(UserDTO userDTO) throws InvalidEmailException, InvalidPasswordException, DuplicateEmailException, UserWithdrawnException {
        
        // 이메일 유효성 검사
        if (!isValidEmail(userDTO.getEmail())) {
            throw new InvalidEmailException("유효하지 않은 이메일 형식입니다: " + userDTO.getEmail());
        }

        if (isInvalidPassword(userDTO.getPassword())) {
            throw new InvalidPasswordException("비밀번호는 대/소문자 포함, 숫자 포함, 5~15자여야 합니다");
        }
        
        // 이메일로 사용자 조회
        UserDTO existingUser = userDAO.findByUserId(userDTO.getUserId());
        if (existingUser != null) {
            // 이미 등록된 사용자인 경우
            if ("ST02".equals(existingUser.getStatus())) {
                throw new UserWithdrawnException("탈퇴한 회원입니다: " + userDTO.getUserId());
            } else {
                throw new DuplicateEmailException("이미 등록된 이메일입니다: " + userDTO.getUserId());
            }
        }

        // 사용자가 명시적으로 상태를 설정하지 않은 경우만 기본값 설정
        if (userDTO.getStatus() == null) {
            userDTO.setStatus("ST00"); // 승인 대기 상태로 설정
        }
        userDTO.setRegisterBy(userDTO.getUserId());
        userDTO.setFirstLoginDate(new Date());
        userDAO.save(userDTO);
    }


    public void modifyUser(UserDTO userDTO) {
        userDAO.modify(userDTO);
    }

    public void changePassword(String userId, String oldPassword, String newPassword)
            throws UserNotFoundException, InvalidPasswordException, AuthenticationException {
        // 사용자 존재 여부 확인
        UserDTO userDTO = userDAO.findByUserId(userId);
        if (userDTO == null) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        // 기존 비밀번호 확인
        if (!oldPassword.equals(userDTO.getPassword())) {
            throw new AuthenticationException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 형식 검증
        if (isInvalidPassword(newPassword)) {
            throw new InvalidPasswordException("비밀번호는 대문자, 소문자, 숫자를 모두 포함하여 5~15자여야 합니다.");
        }

        // 새 비밀번호로 업데이트
        userDTO.setPassword(newPassword);
        log.info(newPassword);
        userDAO.modify(userDTO);
    }

    public boolean requestWithdrawal(String userId) {
        return userDAO.deleteUser(userId);
    }


    private boolean isInvalidPassword(String password) {
        // 대/소문자 포함, 숫자 포함, 5~15자 검증
        return password == null ||
                password.length() < 5 || password.length() > 15 ||
                !password.matches(".*[A-Z].*") || // 대문자 포함
                !password.matches(".*[a-z].*") || // 소문자 포함
                !password.matches(".*[0-9].*");   // 숫자 포함
    }
    
    private boolean isValidEmail(String email) {
        // 이메일 형식 검증: xxx@xxx.xxx 형태
        return email != null &&
                email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    // 모든 사용자 조회
    public List<UserDTO> getAllUsers() {
        return userDAO.findAll();
    }
    
    // 이메일로 사용자 조회
    public UserDTO getUserByEmail(String email) {
        return userDAO.findByUserId(email);
    }
    
    // 사용자 권한 변경 (일반 사용자 -> 관리자, 관리자 -> 일반 사용자)
    public boolean changeUserRole(String email, String newUserType) {
        // 사용자 조회
        UserDTO userDTO = userDAO.findByUserId(email);
        if (userDTO == null) {
            return false;
        }
        
        // 사용자 유형 변경
        userDTO.setUserType(newUserType);
        
        try {
            userDAO.modifyUserRole(userDTO);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}