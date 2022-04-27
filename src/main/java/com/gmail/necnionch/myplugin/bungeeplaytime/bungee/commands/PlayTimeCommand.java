package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayTimeCommand extends Command implements TabExecutor {

    private final BungeePlayTime owner;

    public PlayTimeCommand(BungeePlayTime owner) {
        super("playtime", "bungeeplaytime.command.playtime", "pt");
        this.owner = owner;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("top")) {
                lookupTops(sender);
                return;
            }

            String name = args[0];
            ProxiedPlayer player = owner.getProxy().getPlayer(name);
            if (player != null) {
                lookupPlayer(sender, player.getUniqueId(), player.getName());
            } else {
                owner.fetchPlayerId(name).whenComplete((result, error) -> {
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

        } else if (sender instanceof ProxiedPlayer){
            lookupPlayer(sender, ((ProxiedPlayer) sender).getUniqueId(), sender.getName());

        } else {
            sender.sendMessage(new ComponentBuilder("/pt (player)").color(ChatColor.RED).create());
        }

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return owner.getProxy().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.startsWith(input))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }


    public void lookupPlayer(CommandSender sender, UUID target, String targetName) {
        owner.lookupTime(target).whenComplete((result, error) -> {
            if (error != null) {
                sender.sendMessage(new ComponentBuilder("データエラーです :/").color(ChatColor.RED).create());
                return;
            }

            PlayerTimeResult lookup = result.orElse(null);
            if (lookup != null) {
                owner.lookupTimeRanking(target, new LookupTimeOptions().totalTime(false)).whenComplete((ret, err) -> {
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
        owner.lookupTimeTops(new LookupTimeListOptions().count(8).totalTime(false)).whenComplete((tops, error) -> {
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
