package command;

public interface CommandFactory {
    Command getCommand(String command);
}