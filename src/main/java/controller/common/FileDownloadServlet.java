package controller.common;

import domain.dao.ContentDAO;
import domain.dao.ContentDAOImpl;
import domain.model.Content;
import lombok.extern.slf4j.Slf4j;
import service.FileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@WebServlet("/file/*")
public class FileDownloadServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private FileService fileService;
    private boolean useDbStorage = true; // BLOB 저장 방식 활성화
    
    @Override
    public void init() throws ServletException {
        String uploadPath = getServletContext().getRealPath("/uploads");
        ContentDAO contentDAO = new ContentDAOImpl();
        fileService = new FileService(contentDAO, uploadPath, useDbStorage);
        log.info("FileDownloadServlet 초기화 완료. 업로드 경로: {}", uploadPath);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "파일 ID가 필요합니다.");
            return;
        }
        
        // URL 경로에서 파일 ID 추출 (/file/{fileId})
        String fileId = pathInfo.substring(1);
        
        // 파일 정보 조회
        Content content = fileService.getFileById(fileId);
        if (content == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
            return;
        }
        
        // 이미지 타입 확인 및 응답 설정
        setContentTypeByExtension(response, content.getFileExtension());
        
        if (useDbStorage) {
            // DB에서 직접 바이너리 데이터 제공
            serveFileFromDatabase(response, content);
        } else {
            // 파일 시스템에서 제공
            serveFileFromFileSystem(response, content);
        }
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
     * @param content 파일 컨텐츠 객체
     * @throws IOException I/O 오류 발생 시
     */
    private void serveFileFromDatabase(HttpServletResponse response, Content content) throws IOException {
        if (content.getSaveFile() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일 데이터가 없습니다.");
            return;
        }
        
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(content.getSaveFile());
            outputStream.flush();
        }
    }
    
    /**
     * 파일 시스템에서 파일 제공
     * @param response HTTP 응답
     * @param content 파일 컨텐츠 객체
     * @throws IOException I/O 오류 발생 시
     */
    private void serveFileFromFileSystem(HttpServletResponse response, Content content) throws IOException {
        String filePath = content.getFilePath();
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