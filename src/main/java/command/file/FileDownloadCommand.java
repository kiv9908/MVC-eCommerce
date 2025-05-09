package command.file;

import command.Command;
import config.AppConfig;
import domain.dto.ContentDTO;
import lombok.extern.slf4j.Slf4j;
import service.FileService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class FileDownloadCommand implements Command {
    private final FileService fileService;
    private final boolean useDbStorage;

    public FileDownloadCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.fileService = appConfig.getFileService();

        // fileService가 null이면 로그 남기기
        if (this.fileService == null) {
            log.error("FileService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }

        // DB 저장소 사용 여부 결정 (AppConfig에서 가져오거나 기본값으로 true 설정)
        this.useDbStorage = true;
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // request에서 fileId 가져오기
        String fileId = (String) request.getAttribute("fileId");

        if (fileId == null || fileId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "파일 ID가 필요합니다.");
            return null;
        }

        // 파일 정보 조회
        ContentDTO contentDTO = fileService.getFileById(fileId);
        if (contentDTO == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
            return null;
        }

        // 이미지 타입 확인 및 응답 설정
        setContentTypeByExtension(response, contentDTO.getFileExtension());

        if (useDbStorage) {
            // DB에서 직접 바이너리 데이터 제공
            serveFileFromDatabase(response, contentDTO);
        } else {
            // 파일 시스템에서 제공
            serveFileFromFileSystem(response, contentDTO);
        }

        return null; // 직접 응답 생성하므로 null 반환
    }

    /**
     * 파일 확장자에 따른 Content-Type 설정
     * @param response HTTP 응답
     * @param extension 파일 확장자
     */
    private void setContentTypeByExtension(HttpServletResponse response, String extension) {
        if (extension == null) {
            response.setContentType("application/octet-stream");
            return;
        }

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                response.setContentType("image/jpeg");
                break;
            case "png":
                response.setContentType("image/png");
                break;
            case "gif":
                response.setContentType("image/gif");
                break;
            case "bmp":
                response.setContentType("image/bmp");
                break;
            case "webp":
                response.setContentType("image/webp");
                break;
            default:
                response.setContentType("application/octet-stream");
                break;
        }
    }

    /**
     * DB에 저장된 파일 바이너리 데이터 제공
     * @param response HTTP 응답
     * @param contentDTO 파일 컨텐츠 객체
     * @throws IOException I/O 오류 발생 시
     */
    private void serveFileFromDatabase(HttpServletResponse response, ContentDTO contentDTO) throws IOException {
        if (contentDTO.getSaveFile() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일 데이터가 없습니다.");
            return;
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(contentDTO.getSaveFile());
            outputStream.flush();
        }
    }

    /**
     * 파일 시스템에서 파일 제공
     * @param response HTTP 응답
     * @param contentDTO 파일 컨텐츠 객체
     * @throws IOException I/O 오류 발생 시
     */
    private void serveFileFromFileSystem(HttpServletResponse response, ContentDTO contentDTO) throws IOException {
        String filePath = contentDTO.getFilePath();
        if (filePath == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일 경로가 없습니다.");
            return;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
            return;
        }

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath));
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }
    }
}