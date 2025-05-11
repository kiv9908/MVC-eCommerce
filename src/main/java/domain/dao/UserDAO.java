package domain.dao;

import domain.dto.UserDTO;

import java.util.List;

public interface UserDAO {
    /**
     * 사용자 ID(이메일)로 사용자 조회
     * @param email 사용자 이메일
     * @return 사용자 객체, 없으면 null
     */
    UserDTO findByUserId(String email);

    /**
     * 신규 사용자 저장
     * @param userDTO 저장할 사용자 객체
     */
    void save(UserDTO userDTO);

    /**
     * 사용자 정보 수정
     * @param userDTO 수정할 사용자 객체
     */
    void modify(UserDTO userDTO);

    /**
     * 모든 사용자 조회
     * @return 사용자 목록
     */
    List<UserDTO> findAll();

    /**
     * 사용자 삭제 (또는 상태 변경)
     * @param userId 삭제할 사용자 ID
     * @return 성공 여부
     */
    boolean deleteUser(String userId);
    
    /**
     * 사용자 권한 변경
     * @param userDTO 권한을 변경할 사용자 객체
     */
    void modifyUserRole(UserDTO userDTO);

    /**
     * 페이지네이션을 적용한 사용자 목록 조회
     * @param offset 시작 오프셋
     * @param limit 조회할 항목 수
     * @return 페이지네이션이 적용된 사용자 목록
     */
    List<UserDTO> findAllWithPagination(int offset, int limit);

    /**
     * 전체 사용자 수 조회
     * @return 전체 사용자 수
     */
    int countAll();
}
