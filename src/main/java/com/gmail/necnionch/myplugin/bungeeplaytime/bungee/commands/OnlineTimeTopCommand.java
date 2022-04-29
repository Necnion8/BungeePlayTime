package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;

public class OnlineTimeTopCommand extends Command implements TabExecutor {
    private final OnlineTimeCommand onlineTimeCommand;

    public OnlineTimeTopCommand(OnlineTimeCommand onlineTimeCommand) {
        super("onlinetimetop", "bungeeplaytime.command.onlinetime", "ottop");
        this.onlineTimeCommand = onlineTimeCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        onlineTimeCommand.lookupTops(sender);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }


}
