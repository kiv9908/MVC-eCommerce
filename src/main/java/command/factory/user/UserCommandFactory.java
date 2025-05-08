package command.factory.user;

import command.Command;
import command.CommandFactory;
import command.user.*;

import java.util.HashMap;
import java.util.Map;

public class UserCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public UserCommandFactory() {
        // 사용자 관련 커맨드들 등록
        commandMap.put("join", new JoinCommand());
        commandMap.put("login", new LoginCommand());
        commandMap.put("logout", new LogoutCommand());
        commandMap.put("modify", new ModifyCommand());
        commandMap.put("withdraw", new WithdrawCommand());
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}