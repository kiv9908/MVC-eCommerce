package command.factory.user;

import command.Command;
import command.CommandFactory;
import command.user.product.ProductDetailCommand;
import command.user.product.ProductListCommand;

import java.util.HashMap;
import java.util.Map;

public class UserProductCommandFactory implements CommandFactory {
    private Map<String, Command> commands;

    public UserProductCommandFactory() {
        commands = new HashMap<>();
        initCommands();
    }

    private void initCommands() {
        // 명령어와 해당 Command 객체 매핑
        commands.put("list.do", new ProductListCommand());
        commands.put("detail.do", new ProductDetailCommand());
        // 추가 명령어는 여기에 등록
    }

    @Override
    public Command getCommand(String command) {
        return commands.get(command);
    }
}