package command.admin.product;

import command.Command;
import config.AppConfig;
import domain.dto.ContentDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import service.FileService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class ProductFileInfoCommand implements Command {
    private final FileService fileService;

    public ProductFileInfoCommand() {
        // AppConfig에서 서비스 가져오기
        AppConfig appConfig = AppConfig.getInstance();
        this.fileService = appConfig.getFileService();

        // fileService가 null이면 로그 남기기
        if (this.fileService == null) {
            log.error("FileService가 초기화되지 않았습니다. AppInitializer가 제대로 동작하는지 확인해주세요.");
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileId = request.getParameter("fileId");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                // 파일 ID가 없는 경우
                out.print("{\"error\": \"파일 ID가 없습니다.\"}");
                return null;
            }

            // 파일 정보 조회
            ContentDTO contentDTO = fileService.getFileById(fileId);

            if (contentDTO == null) {
                // 파일을 찾을 수 없는 경우
                out.print("{\"error\": \"파일을 찾을 수 없습니다.\"}");
                return null;
            }

            // JSON 응답 생성
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("fileId", contentDTO.getFileId());
            jsonResponse.put("originalFileName", contentDTO.getOriginalFileName());
            jsonResponse.put("fileExtension", contentDTO.getFileExtension());
            jsonResponse.put("fileType", contentDTO.getFileType());

            // JSON 응답 전송
            out.print(jsonResponse.toJSONString());

        } catch (Exception e) {
            log.error("파일 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            out.print("{\"error\": \"파일 정보 조회 중 오류가 발생했습니다.\"}");
        } finally {
            out.flush();
        }

        return null; // 이미 직접 응답을 작성했으므로 null 반환
    }
}