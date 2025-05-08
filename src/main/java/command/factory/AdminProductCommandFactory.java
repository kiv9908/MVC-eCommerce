package command.factory;

import command.Command;
import command.CommandFactory;
import command.admin.product.*;

import java.util.HashMap;
import java.util.Map;

public class AdminProductCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public AdminProductCommandFactory() {
        // 상품 관리 관련 커맨드 등록
        commandMap.put("list", new ProductListCommand());
        commandMap.put("create", new ProductCreateCommand());  // GET/POST 모두 처리
        commandMap.put("edit", new ProductEditCommand());    // GET/POST 모두 처리
        commandMap.put("delete", new ProductDeleteCommand());
        commandMap.put("file-info", new ProductFileInfoCommand());
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}