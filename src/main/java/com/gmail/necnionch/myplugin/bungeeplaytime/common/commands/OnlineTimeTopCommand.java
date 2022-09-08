package com.gmail.necnionch.myplugin.bungeeplaytime.common.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.SimpleCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class OnlineTimeTopCommand extends SimpleCommand {
    private final OnlineTimeCommand onlineTimeCommand;

    public OnlineTimeTopCommand(OnlineTimeCommand onlineTimeCommand) {
        super("onlinetimetop", "bungeeplaytime.command.onlinetime", "ottop");
        this.onlineTimeCommand = onlineTimeCommand;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        onlineTimeCommand.lookupTops(sender);
    }

    @Override
    @NotNull
    public List<String> tabComplete(CommandSender sender, String c, List<String> args) {
        return Collections.emptyList();
    }


}
