package service;

import domain.dao.ContentDAO;
import domain.model.Content;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class FileService {
    private final ContentDAO contentDAO;
    private final String uploadPath;
    private final boolean useDbStorage;  // BLOB 저장 여부
    
    public FileService(ContentDAO contentDAO, String uploadPath, boolean useDbStorage) {
        this.contentDAO = contentDAO;
        this.uploadPath = uploadPath;
        this.useDbStorage = useDbStorage;
        
        // 업로드 디렉터리 생성
        createUploadDirectoryIfNotExists();
    }
    
    private void createUploadDirectoryIfNotExists() {
        if (!useDbStorage) {  // 파일 시스템에 저장하는 경우만 체크
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("업로드 디렉터리 생성: {}", uploadPath);
                } else {
                    log.error("업로드 디렉터리 생성 실패: {}", uploadPath);
                }
            }
        }
    }
    
    /**
     * 파일 업로드 처리 메서드
     * @param filePart 업로드된 파일 Part
     * @param serviceId 서비스 ID (예: "product")
     * @param registerNo 등록자 ID
     * @return 생성된 파일 ID
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public String uploadFile(Part filePart, String serviceId, String registerNo) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            log.error("업로드 파일이 없거나 크기가 0입니다.");
            return null;
        }
        
        String originalFileName = getFileName(filePart);
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            log.error("파일명을 추출할 수 없습니다.");
            return null;
        }
        
        String fileExtension = FilenameUtils.getExtension(originalFileName);
        if (!isValidImageExtension(fileExtension)) {
            log.error("지원하지 않는 파일 형식입니다: {}", fileExtension);
            return null;
        }
        
        // UUID로 고유한 저장 파일명 생성
        String savedFileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        Content content = new Content();
        content.setOriginalFileName(originalFileName);
        content.setSavedFileName(savedFileName);
        content.setFileExtension(fileExtension);
        content.setFileType(determineFileType(fileExtension));
        content.setServiceId(serviceId);
        content.setRegisterNo(registerNo);
        content.setSaveDate(new Date());
        content.setHitCount(0);
        content.setFirstDate(new Date());
        
        if (useDbStorage) {
            // BLOB으로 DB에 저장
            try (InputStream inputStream = filePart.getInputStream()) {
                content.setSaveFile(IOUtils.toByteArray(inputStream));
            }
        } else {
            // 파일 시스템에 저장
            Path filePath = Paths.get(uploadPath, savedFileName);
            content.setFilePath(filePath.toString());
            
            // 파일 저장
            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, filePath);
            }
        }
        
        // DB에 메타데이터 저장
        String fileId = contentDAO.save(content);
        log.info("파일 업로드 완료: {} (ID: {})", originalFileName, fileId);
        
        return fileId;
    }
    
    /**
     * 파일 ID로 파일 조회
     * @param fileId 파일 ID
     * @return Content 객체
     */
    public Content getFileById(String fileId) {
        Content content = contentDAO.findByFileId(fileId);
        if (content != null) {
            // 조회수 증가
            contentDAO.incrementHitCount(fileId);
        }
        return content;
    }
    
    /**
     * 서비스 ID로 파일 목록 조회
     * @param serviceId 서비스 ID
     * @return Content 목록
     */
    public List<Content> getFilesByServiceId(String serviceId) {
        return contentDAO.findByServiceId(serviceId);
    }
    
    /**
     * 파일 삭제
     * @param fileId 파일 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(String fileId) {
        Content content = contentDAO.findByFileId(fileId);
        if (content == null) {
            log.error("삭제할 파일을 찾을 수 없습니다: {}", fileId);
            return false;
        }
        
        // 파일 시스템에서 삭제 (필요한 경우)
        if (!useDbStorage && content.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(content.getFilePath()));
            } catch (IOException e) {
                log.error("파일 삭제 실패: {}", content.getFilePath(), e);
            }
        }
        
        // DB에서 레코드 삭제
        return contentDAO.delete(fileId);
    }
    
    /**
     * Part에서 파일명 추출
     * @param part 파일 Part
     * @return 파일명
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] elements = contentDisposition.split(";");
        
        for (String element : elements) {
            if (element.trim().startsWith("filename")) {
                return element.substring(element.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
    
    /**
     * 파일 타입 결정 (이미지, 문서 등)
     * @param fileExtension 파일 확장자
     * @return 파일 타입 코드
     */
    private String determineFileType(String fileExtension) {
        if (fileExtension == null) {
            return "ETC";
        }
        
        switch (fileExtension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
                return "IMG";
                
            case "doc":
            case "docx":
            case "xls":
            case "xlsx":
            case "ppt":
            case "pptx":
            case "pdf":
            case "txt":
                return "DOC";
                
            case "mp3":
            case "wav":
            case "ogg":
                return "AUD";
                
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
                return "VID";
                
            default:
                return "ETC";
        }
    }
    
    /**
     * 유효한 이미지 확장자인지 검증
     * @param extension 파일 확장자
     * @return 유효 여부
     */
    private boolean isValidImageExtension(String extension) {
        if (extension == null) {
            return false;
        }
        
        String[] validExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        for (String validExt : validExtensions) {
            if (validExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}