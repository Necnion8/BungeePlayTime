package com.gmail.necnionch.myplugin.bungeeplaytime.common.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.CommandPlatform;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.IPlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.SimpleCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlayTimeCommand extends SimpleCommand {

    private final IPlayTimeAPI api;
    private final CommandPlatform platform;

    public PlayTimeCommand(IPlayTimeAPI api, CommandPlatform platform) {
        super("playtime", "bungeeplaytime.command.playtime", "pt");
        this.api = api;
        this.platform = platform;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!args.isEmpty()) {
            if (args.get(0).equalsIgnoreCase("top")) {
                lookupTops(sender);
                return;
            }

            String name = args.get(0);

            CommandSender player = platform.getPlayer(name).orElse(null);
            if (player != null) {
                lookupPlayer(sender, player.getPlayerUniqueId(), player.getName());
            } else {
                api.fetchPlayerId(name).whenComplete((result, error) -> {
                    if (error != null) {
                        sender.sendMessage(new ComponentBuilder("データエラーです :/").color(ChatColor.RED).create());
                        return;
                    }

                    PlayerName playerName = result.orElse(null);
                    if (playerName == null) {
                        sender.sendMessage(new ComponentBuilder("プレイヤーが見つかりません :/").color(ChatColor.RED).create());
                        return;
                    }

                    lookupPlayer(sender, playerName.getUniqueId(), playerName.getName());
                });
            }

        } else if (sender.getPlayerUniqueId() != null){
            lookupPlayer(sender, sender.getPlayerUniqueId(), sender.getName());

        } else {
            sender.sendMessage(new ComponentBuilder("/pt (player)").color(ChatColor.RED).create());
        }

    }

    @Override
    @NotNull
    public List<String> tabComplete(CommandSender sender, String c, List<String> args) {
        if (args.size() == 1) {
            String input = args.get(0).toLowerCase(Locale.ROOT);
            return platform.getOnlinePlayers().stream()
                    .map(CommandSender::getName)
                    .filter(name -> name.startsWith(input))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    public void lookupPlayer(CommandSender sender, UUID target, String targetName) {
        api.lookupTime(target).whenComplete((result, error) -> {
            if (error != null) {
                sender.sendMessage(new ComponentBuilder("データエラーです :/").color(ChatColor.RED).create());
                return;
            }

            PlayerTimeResult lookup = result.orElse(null);
            if (lookup != null) {
                api.lookupTimeRanking(target, new LookupTimeOptions().totalTime(false).currentServer()).whenComplete((ret, err) -> {
                    String formattedTime = BPTUtil.formatTimeText(lookup.getPlayTime());
                    String message;
                    if (err == null && ret.isPresent()) {
                        int ranking = ret.getAsInt() + 1;
                        message = String.format("§7[§f§o§li§7] §7%sさんのプレイ時間は %s §7です §e(#%d)", targetName, formattedTime, ranking);
                    } else {
                        message = String.format("§7[§f§o§li§7] §7%sさんのプレイ時間は %s §7です", targetName, formattedTime);
                    }
                    sender.sendMessage(TextComponent.fromLegacyText(message));
                });

            } else {
                sender.sendMessage(new ComponentBuilder("データがありません :/").color(ChatColor.RED).create());
            }
        });

    }

    public void lookupTops(CommandSender sender) {
        api.lookupTimeTops(new LookupTimeListOptions().count(8).totalTime(false).currentServer()).whenComplete((tops, error) -> {
            if (error != null) {
                sender.sendMessage(new ComponentBuilder("データエラーです :/").color(ChatColor.RED).create());
                return;
            }

            if (tops.getEntries().isEmpty()) {
                sender.sendMessage(new ComponentBuilder("データがありません :/").color(ChatColor.RED).create());
                return;
            }
            int maxNameSize = 12;
            tops.fetchNames(api::fetchPlayerName).whenComplete((names, ex) -> {
                try {
                    ComponentBuilder b = new ComponentBuilder();
                    b.append("==== ").color(ChatColor.GRAY);
                    b.append("プレイ時間トップ").color(ChatColor.GOLD);
                    b.append(" ====\n").color(ChatColor.GRAY);

                    int ranking = 1;
                    for (PlayerTimeResult entry : tops.getEntries()) {
                        Optional<String> name = names.get(entry.getPlayerId());

                        int spaceSize = Math.max(0, maxNameSize - name.orElse("n/a").length());
                        String space = new String(new char[spaceSize]).replace("\0", " ");
                        b.append(ranking + ". " + name.orElse("n/a") + space)
                                .color(name.isPresent() ? ChatColor.GOLD : ChatColor.GRAY);
                        b.append(": ").color(ChatColor.GRAY);

                        b.appendLegacy(BPTUtil.formatTimeText(entry.getPlayTime()) + "\n");
                        ranking++;
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
