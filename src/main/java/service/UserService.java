package service;

import domain.model.User;
import domain.repository.UserRepository;
import exception.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(User user) throws InvalidEmailException, InvalidPasswordException, DuplicateEmailException, UserWithdrawnException {
        
        // 이메일 유효성 검사
        if (!isValidEmail(user.getEmail())) {
            throw new InvalidEmailException("유효하지 않은 이메일 형식입니다: " + user.getEmail());
        }

        if (!isValidPassword(user.getPassword())) {
            throw new InvalidPasswordException("비밀번호는 대/소문자 포함, 숫자 포함, 5~15자여야 합니다");
        }
        
        // 이메일로 사용자 조회
        User existingUser = userRepository.findByUserId(user.getUserId());
        if (existingUser != null) {
            // 이미 등록된 사용자인 경우
            if ("ST02".equals(existingUser.getStatus())) {
                throw new UserWithdrawnException("탈퇴한 회원입니다: " + user.getUserId());
            } else {
                throw new DuplicateEmailException("이미 등록된 이메일입니다: " + user.getUserId());
            }
        }

        user.setStatus("ST01");
        user.setRegisterBy(user.getUserId());
        user.setFirstLoginDate(new Date());
        userRepository.save(user);
    }


    public void updateUser(User user) {
        userRepository.update(user);
    }

    public void changePassword(String userId, String oldPassword, String newPassword)
            throws UserNotFoundException, InvalidPasswordException, AuthenticationException {
        // 사용자 존재 여부 확인
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        // 기존 비밀번호 확인
        if (!oldPassword.equals(user.getPassword())) {
            throw new AuthenticationException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 형식 검증
        if (!isValidPassword(newPassword)) {
            throw new InvalidPasswordException("비밀번호는 대문자, 소문자, 숫자를 모두 포함하여 5~15자여야 합니다.");
        }

        userRepository.update(user);
    }

    public boolean requestWithdrawal(String userId) {
        return userRepository.deleteUser(userId);
    }


    private boolean isValidPassword(String password) {
        // 대/소문자 포함, 숫자 포함, 5~15자 검증
        return password != null &&
                password.length() >= 5 && password.length() <= 15 &&
                password.matches(".*[A-Z].*") && // 대문자 포함
                password.matches(".*[a-z].*") && // 소문자 포함
                password.matches(".*[0-9].*");   // 숫자 포함
    }
    
    private boolean isValidEmail(String email) {
        // 이메일 형식 검증: xxx@xxx.xxx 형태
        return email != null &&
                email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    // 모든 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 이메일로 사용자 조회
    public User getUserByEmail(String email) {
        return userRepository.findByUserId(email);
    }
    
    // 사용자 권한 변경 (일반 사용자 -> 관리자, 관리자 -> 일반 사용자)
    public boolean changeUserRole(String email, String newUserType) {
        // 사용자 조회
        User user = userRepository.findByUserId(email);
        if (user == null) {
            return false;
        }
        
        // 사용자 유형 변경
        user.setUserType(newUserType);
        
        try {
            userRepository.updateUserRole(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}