package command.factory.user;

import command.Command;
import command.CommandFactory;
import command.user.basket.*;


public class UserBasketCommandFactory implements CommandFactory {

    @Override
    public Command getCommand(String command) {
        Command cmd = null;

        switch (command) {
            case "list":
                cmd = new BasketListCommand();
                break;
            case "add":
                cmd = new BasketAddCommand();
                break;
            case "update":
                cmd = new BasketUpdateCommand();
                break;
            case "delete":
                cmd = new BasketDeleteCommand();
                break;
            case "clear":
                cmd = new BasketClearCommand();
                break;
            default:
                break;
        }

        return cmd;
    }
}