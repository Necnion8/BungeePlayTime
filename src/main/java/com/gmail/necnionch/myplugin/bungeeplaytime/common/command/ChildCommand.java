package com.gmail.necnionch.myplugin.bungeeplaytime.common.command;

public class ChildCommand extends RootCommand {
    private final String name;

    public ChildCommand(String name) {
        this.name = name;
    }

    public Command build() {
        return new Command(name, null, this::execute, this::tabComplete);
    }

}
