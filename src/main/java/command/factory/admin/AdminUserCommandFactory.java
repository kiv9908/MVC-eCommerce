package command.factory.admin;

import command.Command;
import command.CommandFactory;
import command.admin.user.*;

import java.util.HashMap;
import java.util.Map;

public class AdminUserCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public AdminUserCommandFactory() {
        // 회원 관리 관련 커맨드 등록
        commandMap.put("list", new AdminUserListCommand());
        commandMap.put("edit", new AdminUserEditCommand());     // 새로운 커맨드 추가
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}