package controller.file;

import command.Command;
import command.factory.file.FileDownloadCommandFactory;
import controller.AbstractDomainController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebServlet("/file/*")
public class FileDownloadController extends AbstractDomainController {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        this.domainPath = "file";
        this.commandFactory = new FileDownloadCommandFactory();
        log.info("FileDownloadController 초기화 완료");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // URL에서 파일 ID 추출 및 request에 설정
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            String fileId = pathInfo.substring(1);
            request.setAttribute("fileId", fileId);
        }

        // 항상 download 명령어 실행 (기본 파일 다운로드)
        try {
            Command cmd = commandFactory.getCommand("download");
            if (cmd == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.");
                return;
            }

            cmd.execute(request, response);
        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "파일 다운로드 중 오류가 발생했습니다.");
        }
    }
}