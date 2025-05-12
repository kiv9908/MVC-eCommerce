package command.factory.admin;

import command.Command;
import command.CommandFactory;
import command.admin.order.OrderDetailCommand;
import command.admin.order.OrderListCommand;
import command.admin.order.UpdateOrderStatusCommand;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 관련 Command 객체를 생성하는 Factory 클래스
 */
@Slf4j
public class AdminOrderCommandFactory implements CommandFactory {

    @Override
    public Command getCommand(String command) {
        log.info("OrderCommandFactory: " + command + " 명령어 처리");

        // 주문 관련 명령어에 따라 적절한 Command 객체 반환
        if (command == null) {
            return null;
        }

        switch (command) {
            case "list":
                return new OrderListCommand();
            case "detail":
                return new OrderDetailCommand();
            case "update":
                return new UpdateOrderStatusCommand();
            default:
                log.warn("지원하지 않는 주문 명령어: " + command);
                return null;
        }
    }
}