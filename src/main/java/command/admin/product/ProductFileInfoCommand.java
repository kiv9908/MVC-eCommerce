package command.admin.product;

import command.Command;
import domain.dao.ContentDAO;
import domain.dao.ContentDAOImpl;
import domain.model.Content;
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
    private FileService fileService;
    private boolean useDbStorage = true;

    public ProductFileInfoCommand() {
        // 파일 서비스 초기화 - ServletContext에서 업로드 경로를 가져올 수 없으므로, 생성자에서는 null로 초기화
        ContentDAO contentDAO = new ContentDAOImpl();
        fileService = null; // execute 메소드에서 초기화 예정
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 파일 서비스가 아직 초기화되지 않은 경우 초기화
        if (fileService == null) {
            String uploadPath = request.getServletContext().getRealPath("/uploads");
            ContentDAO contentDAO = new ContentDAOImpl();
            fileService = new FileService(contentDAO, uploadPath, useDbStorage);
        }

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
            Content content = fileService.getFileById(fileId);

            if (content == null) {
                // 파일을 찾을 수 없는 경우
                out.print("{\"error\": \"파일을 찾을 수 없습니다.\"}");
                return null;
            }

            // JSON 응답 생성
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("fileId", content.getFileId());
            jsonResponse.put("originalFileName", content.getOriginalFileName());
            jsonResponse.put("fileExtension", content.getFileExtension());
            jsonResponse.put("fileType", content.getFileType());

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