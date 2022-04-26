package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;

public class PlayTimeTopCommand extends Command implements TabExecutor {
    private final PlayTimeCommand playTimeCommand;

    public PlayTimeTopCommand(PlayTimeCommand playTimeCommand) {
        super("playtimetop", "bungeeplaytime.command.playtime", "pttop");
        this.playTimeCommand = playTimeCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        playTimeCommand.lookupTops(sender);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }


}
