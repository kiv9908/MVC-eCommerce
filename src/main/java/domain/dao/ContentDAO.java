package domain.dao;

import domain.model.Content;

import java.util.List;

public interface ContentDAO {
    // 파일 ID로 컨텐츠 조회
    Content findByFileId(String fileId);
    
    // 서비스 ID로 컨텐츠 목록 조회
    List<Content> findByServiceId(String serviceId);
    
    // 컨텐츠 저장
    String save(Content content);
    
    // 컨텐츠 수정
    boolean update(Content content);
    
    // 컨텐츠 삭제
    boolean delete(String fileId);
    
    // 조회수 증가
    boolean incrementHitCount(String fileId);
}