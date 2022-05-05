package com.gmail.necnionch.myplugin.bungeeplaytime.common.command;

public abstract class SimpleCommand implements Command.Executor, Command.TabCompleter {

    private final String name;
    private final String permission;
    private final String[] aliases;

    public SimpleCommand(String name, String permission, String... aliases) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getAliases() {
        return aliases;
    }

}
