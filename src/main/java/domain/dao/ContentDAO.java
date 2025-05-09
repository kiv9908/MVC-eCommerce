package domain.dao;

import domain.dto.ContentDTO;

import java.util.List;

public interface ContentDAO {
    // 파일 ID로 컨텐츠 조회
    ContentDTO findByFileId(String fileId);
    
    // 서비스 ID로 컨텐츠 목록 조회
    List<ContentDTO> findByServiceId(String serviceId);
    
    // 컨텐츠 저장
    String save(ContentDTO contentDTO);
    
    // 컨텐츠 수정
    boolean update(ContentDTO contentDTO);
    
    // 컨텐츠 삭제
    boolean delete(String fileId);
    
    // 조회수 증가
    boolean incrementHitCount(String fileId);
}