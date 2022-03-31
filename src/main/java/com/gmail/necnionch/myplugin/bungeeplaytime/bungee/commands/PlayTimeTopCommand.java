package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.LookupTop;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.Optional;

public class PlayTimeTopCommand extends Command implements TabExecutor {

    private final BungeePlayTime owner;

    public PlayTimeTopCommand(BungeePlayTime owner) {
        super("playtimetop", "bungeeplaytime.command.playtime", "pttop");
        this.owner = owner;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        lookupTops(sender);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }


    private void lookupTops(CommandSender sender) {
        owner.lookupTimeTops(10, false).whenComplete((tops, error) -> {
            if (error != null) {
                sender.sendMessage(new ComponentBuilder("データエラーです :/").color(ChatColor.RED).create());
                return;
            }

            if (tops.getEntries().isEmpty()) {
                sender.sendMessage(new ComponentBuilder("データがありません :/").color(ChatColor.RED).create());
                return;
            }
            int maxNameSize = 12;
            tops.fetchNames(owner::fetchPlayerName).whenComplete((names, ex) -> {
                try {
                    ComponentBuilder b = new ComponentBuilder();
                    b.append("==== ").color(ChatColor.GRAY);
                    b.append("プレイ時間トップ").color(ChatColor.GOLD);
                    b.append(" ====\n").color(ChatColor.GRAY);

                    for (LookupTop.Entry entry : tops.getEntries()) {
                        Optional<String> name = names.get(entry.getUniqueId());

                        int spaceSize = Math.max(0, maxNameSize - name.orElse("n/a").length());
                        String space = new String(new char[spaceSize]).replace("\0", " ");
                        b.append(" " + name.orElse("n/a") + space)
                                .color(name.isPresent() ? ChatColor.GOLD : ChatColor.GRAY);
                        b.append(": ").color(ChatColor.GRAY);

                        b.appendLegacy(owner.formatTimeText(entry.getPlayedTime()) + "\n");
                    }
                    b.append("=============================").color(ChatColor.GRAY);
                    sender.sendMessage(b.create());

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        });
    }


}
