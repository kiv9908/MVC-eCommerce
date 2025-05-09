package command.factory.file;

import command.Command;
import command.CommandFactory;
import command.file.FileDownloadCommand;

import java.util.HashMap;
import java.util.Map;

public class FileDownloadCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public FileDownloadCommandFactory() {
        // 파일 다운로드 Command 등록
        commandMap.put("download", new FileDownloadCommand());
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}