package command.factory.admin;

import command.Command;
import command.CommandFactory;
import command.admin.mapping.MappingCreateCommand;
import command.admin.mapping.MappingDeleteCommand;
import command.admin.mapping.MappingEditCommand;
import command.admin.mapping.MappingListCommand;


import java.util.HashMap;
import java.util.Map;

public class AdminMappingCommandFactory implements CommandFactory {

    private Map<String, Command> commandMap = new HashMap<>();

    // Initialize the command map in the constructor
    public AdminMappingCommandFactory() {
        commandMap.put("list", new MappingListCommand());
        commandMap.put("edit", new MappingEditCommand());
        commandMap.put("create", new MappingCreateCommand());
        commandMap.put("delete", new MappingDeleteCommand());
    }

    @Override
    public Command getCommand(String command) {
        return commandMap.getOrDefault(command, null);
    }
}