package service;

import config.AppConfig;
import domain.dao.ContentDAO;
import domain.dto.ContentDTO;
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

    public FileService(String uploadPath, boolean useDbStorage) {
        log.info("FileService 초기화: uploadPath={}, useDbStorage={}", uploadPath, useDbStorage);

        AppConfig appConfig = AppConfig.getInstance();
        this.contentDAO = appConfig.getContentDAO();
        this.uploadPath = uploadPath;
        this.useDbStorage = useDbStorage;

        // 업로드 디렉터리 생성
        createUploadDirectoryIfNotExists();
    }

    private void createUploadDirectoryIfNotExists() {
        log.debug("업로드 디렉터리 확인: {}", uploadPath);

        if (uploadPath == null || uploadPath.isEmpty()) {
            log.error("업로드 경로가 비어있습니다. 서블릿 컨텍스트가 올바르게 초기화되었는지 확인하세요.");
            return;
        }

        if (!useDbStorage) {  // 파일 시스템에 저장하는 경우만 체크
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("업로드 디렉터리 생성 성공: {}", uploadPath);
                } else {
                    log.error("업로드 디렉터리 생성 실패: {} (권한 문제 또는 경로 오류)", uploadPath);
                }
            } else {
                log.info("업로드 디렉터리가 이미 존재합니다: {}", uploadPath);
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

        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setOriginalFileName(originalFileName);
        contentDTO.setSavedFileName(savedFileName);
        contentDTO.setFileExtension(fileExtension);
        contentDTO.setFileType(determineFileType(fileExtension));
        contentDTO.setServiceId(serviceId);
        contentDTO.setRegisterNo(registerNo);
        contentDTO.setSaveDate(new Date());
        contentDTO.setHitCount(0);
        contentDTO.setFirstDate(new Date());

        if (useDbStorage) {
            // BLOB으로 DB에 저장
            try (InputStream inputStream = filePart.getInputStream()) {
                contentDTO.setSaveFile(IOUtils.toByteArray(inputStream));
            }
        } else {
            // 파일 시스템에 저장
            Path filePath = Paths.get(uploadPath, savedFileName);
            contentDTO.setFilePath(filePath.toString());

            // 파일 저장
            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, filePath);
            }
        }

        // DB에 메타데이터 저장
        String fileId = contentDAO.save(contentDTO);
        log.info("파일 업로드 완료: {} (ID: {})", originalFileName, fileId);

        return fileId;
    }

    /**
     * 파일 ID로 파일 조회
     * @param fileId 파일 ID
     * @return ContentDTO 객체
     */
    public ContentDTO getFileById(String fileId) {

        if (fileId == null || fileId.isEmpty()) {
            log.error("파일 ID가 null이거나 비어 있습니다.");
            return null;
        }

        ContentDTO contentDTO = contentDAO.findByFileId(fileId);

        if (contentDTO == null) {
            log.error("파일 ID {}에 해당하는 컨텐츠를 찾을 수 없습니다.", fileId);
            return null;
        }

        // 조회수 증가
        contentDAO.incrementHitCount(fileId);

        return contentDTO;
    }

    /**
     * 서비스 ID로 파일 목록 조회
     * @param serviceId 서비스 ID
     * @return ContentDTO 목록
     */
    public List<ContentDTO> getFilesByServiceId(String serviceId) {
        return contentDAO.findByServiceId(serviceId);
    }

    /**
     * 파일 삭제
     * @param fileId 파일 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(String fileId) {
        ContentDTO contentDTO = contentDAO.findByFileId(fileId);
        if (contentDTO == null) {
            log.error("삭제할 파일을 찾을 수 없습니다: {}", fileId);
            return false;
        }

        // 파일 시스템에서 삭제 (필요한 경우)
        if (!useDbStorage && contentDTO.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(contentDTO.getFilePath()));
            } catch (IOException e) {
                log.error("파일 삭제 실패: {}", contentDTO.getFilePath(), e);
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