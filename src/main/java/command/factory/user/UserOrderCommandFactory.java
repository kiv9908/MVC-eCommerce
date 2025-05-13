package command.factory.user;

import command.Command;
import command.CommandFactory;
import command.user.order.OrderCreateCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 주문 관련 Command를 관리하는 Factory 클래스
 */
public class UserOrderCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public UserOrderCommandFactory() {
        // 주문 관련 커맨드들 등록
        commandMap.put("order.do", new OrderCreateCommand()); // 주문 생성
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}