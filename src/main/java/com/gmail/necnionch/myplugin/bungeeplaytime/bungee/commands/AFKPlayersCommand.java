package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayerTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AFKPlayersCommand extends Command implements TabExecutor {

    private final BungeePlayTime owner;

    public AFKPlayersCommand(BungeePlayTime owner) {
        super("afkplayers", "bungeeplaytime.command.afkplayers");
        this.owner = owner;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<PlayerTime> players = owner.getPlayers().stream()
                .filter(PlayerTime::isAFK)
                .sorted(Comparator.comparingLong(p -> p.getStartTime() - System.currentTimeMillis()))
                .collect(Collectors.toList());

        if (players.isEmpty()) {
            sender.sendMessage(new ComponentBuilder("放置中のプレイヤーは居ません").color(ChatColor.RED).create());
            return;
        }

        ComponentBuilder b = new ComponentBuilder();
        b.append("====== ").color(ChatColor.GRAY);
        b.append("AFK プレイヤー").color(ChatColor.GOLD);
        b.append(" ======\n").color(ChatColor.GRAY);

        int maxNameSize = 12;
        for (PlayerTime time : players) {
            String name = time.getPlayer().getName();

            int spaceSize = Math.max(0, maxNameSize - name.length());
            String space = new String(new char[spaceSize]).replace("\0", " ");
            b.append(" " + name + space).color(ChatColor.GOLD);
            b.append(": ").color(ChatColor.GRAY);

            b.appendLegacy(BPTUtil.formatTimeText(System.currentTimeMillis() - time.getStartTime()) + "\n");
        }
        b.append("=============================").color(ChatColor.GRAY);
        sender.sendMessage(b.create());
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptySet();
    }


}
