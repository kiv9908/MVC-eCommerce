package command.factory.admin;

import command.Command;
import command.CommandFactory;
import command.admin.category.*;

import java.util.HashMap;
import java.util.Map;

public class AdminCategoryCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public AdminCategoryCommandFactory() {
        // 카테고리 관리 관련 커맨드 등록
        commandMap.put("list", new CategoryListCommand());
        commandMap.put("create", new CategoryCreateCommand());  // GET/POST 모두 처리
        commandMap.put("edit", new CategoryEditCommand());    // GET/POST 모두 처리
        commandMap.put("delete", new CategoryDeleteCommand());

    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}