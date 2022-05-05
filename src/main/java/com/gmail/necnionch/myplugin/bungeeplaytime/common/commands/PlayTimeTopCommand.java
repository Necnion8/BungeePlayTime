package com.gmail.necnionch.myplugin.bungeeplaytime.common.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.SimpleCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class PlayTimeTopCommand extends SimpleCommand {
    private final PlayTimeCommand playTimeCommand;

    public PlayTimeTopCommand(PlayTimeCommand playTimeCommand) {
        super("playtimetop", "bungeeplaytime.command.playtime", "pttop");
        this.playTimeCommand = playTimeCommand;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        playTimeCommand.lookupTops(sender);
    }

    @Override
    @NotNull
    public List<String> tabComplete(CommandSender sender, String c, List<String> args) {
        return Collections.emptyList();
    }


}
