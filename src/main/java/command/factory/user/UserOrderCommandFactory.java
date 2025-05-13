package command.factory.user;

import command.Command;
import command.CommandFactory;
import command.user.order.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 주문 관련 Command를 관리하는 Factory 클래스
 */
public class UserOrderCommandFactory implements CommandFactory {
    private Map<String, Command> commandMap = new HashMap<>();

    public UserOrderCommandFactory() {
        // 기존 OrderCreateCommand를 ShowOrderFormCommand와 CreateOrderCommand로 분리
        commandMap.put("form.do", new ShowOrderFormCommand());  // 주문 양식 표시
        commandMap.put("create.do", new OrderCreateCommand());  // 주문 생성 처리
        commandMap.put("list.do", new OrderListCommand());      // 주문 목록 조회
        commandMap.put("detail.do", new OrderDetailCommand());  // 주문 상세 조회
        commandMap.put("cancel.do", new OrderCancelCommand());  // 주문 취소
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.get(command);
    }
}