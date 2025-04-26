package domain.repository;

import domain.model.User;

import java.util.List;

public interface UserRepository {
    /**
     * 사용자 ID(이메일)로 사용자 조회
     * @param email 사용자 이메일
     * @return 사용자 객체, 없으면 null
     */
    User findByUserId(String email);

    /**
     * 신규 사용자 저장
     * @param user 저장할 사용자 객체
     */
    void save(User user);

    /**
     * 사용자 정보 업데이트
     * @param user 업데이트할 사용자 객체
     */
    void update(User user);

    /**
     * 모든 사용자 조회
     * @return 사용자 목록
     */
    List<User> findAll();

    /**
     * 사용자 삭제 (또는 상태 변경)
     * @param userId 삭제할 사용자 ID
     * @return 성공 여부
     */
    boolean deleteUser(String userId);
    
    /**
     * 사용자 권한 변경
     * @param user 권한을 변경할 사용자 객체
     */
    void updateUserRole(User user);
}
