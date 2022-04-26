package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.ChildCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandBungee;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.RootCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainCommand extends RootCommand {
    private final BungeePlayTime plugin;

    public MainCommand(BungeePlayTime plugin) {
        this.plugin = plugin;
        ChildCommand debug = new ChildCommand("debug");
        debug.addCommand("actives", null, this::execDebugActives);
        debug.addCommand("bridgeretry", null, this::execDebugBridgeRetry);
        addCommand(debug.build());
        addCommand("reload", null, this::execReload);
        addCommand("info", null, this::execInfo);
    }

    public void registerCommand() {
        Command command = CommandBungee.build(this, "bungeeplaytime", "bungeeplaytime.command.bungeeplaytime");
        plugin.getProxy().getPluginManager().registerCommand(plugin, command);
    }


    private void execDebugActives(CommandSender s, List<String> args) {
        String actives = plugin.getMessenger().actives().stream()
                .map(msg -> msg.getServerInfo().getName())
                .collect(Collectors.joining(ChatColor.GRAY + ", " + ChatColor.WHITE));

        s.sendMessage(new ComponentBuilder("連携済みサーバー: ").color(ChatColor.GOLD)
                .appendLegacy(actives)
                .create());
    }

    private void execDebugBridgeRetry(CommandSender s, List<String> args) {
        Set<ServerInfo> servers = plugin.getProxy().getServers().values().stream()
                .filter(svr -> !svr.getPlayers().isEmpty())
                .collect(Collectors.toSet());

        if (servers.isEmpty()) {
            s.sendMessage(new ComponentBuilder("プレイヤーが接続していないため、実行できません。").color(ChatColor.RED).create());
            return;
        }

        s.sendMessage(new ComponentBuilder("サーバーとの再連携を試みます...").color(ChatColor.GOLD).create());

        plugin.getMessenger().sendPingAll();
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (plugin.getMessenger().actives().isEmpty()) {
                s.sendMessage(new ComponentBuilder("サーバー連携されませんでした。").color(ChatColor.RED).create());
                return;
            }
            String actives = plugin.getMessenger().actives().stream()
                    .map(msg -> msg.getServerInfo().getName())
                    .collect(Collectors.joining(ChatColor.GRAY + ", " + ChatColor.WHITE));
            s.sendMessage(new ComponentBuilder("連携したサーバー: ").color(ChatColor.GOLD)
                    .appendLegacy(actives)
                    .create());
        }, 5250, TimeUnit.MILLISECONDS);
    }

    private void execReload(CommandSender s, List<String> args) {
        ComponentBuilder b = new ComponentBuilder();

        if (plugin.getMainConfig().load()) {
            boolean dbResult;
            try {
                dbResult = plugin.connectDatabase();
            } catch (Throwable e) {
                e.printStackTrace();
                dbResult = false;
            }

            if (dbResult) {
                s.sendMessage(b.append("設定ファイルを再読み込みしました。").color(ChatColor.GOLD).create());
            } else {
                s.sendMessage(b.append("再読み込みしましたが、データベースに再接続できません。").color(ChatColor.RED).create());
            }
            plugin.sendSettingAll();

        } else {
            s.sendMessage(b.append("再読み込みできませんでした。").color(ChatColor.RED).create());
        }

    }

    private void execInfo(CommandSender s, List<String> args) {
        ComponentBuilder b = new ComponentBuilder();

        if (args.isEmpty()) {
            s.sendMessage(b.append("プレイヤーを指定してください").color(ChatColor.RED).create());
            return;
        }

        Consumer<PlayerName> foundName = (playerName) -> {
//            plugin.lookupFirstTime()  todo
        };

        String name = args.remove(0);
        UUID uuid;
        try {
            uuid = UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            plugin.fetchPlayerId(name).whenComplete((ret, err) -> {
                if (err != null || !ret.isPresent()) {
                    s.sendMessage(b.append("内部エラーが発生しました").color(ChatColor.RED).create());
                } else {
                    foundName.accept(ret.get());
                }
            });
            return;
        }
        plugin.fetchPlayerName(uuid).whenComplete((ret, err) -> {
            if (err != null || !ret.isPresent()) {
                s.sendMessage(b.append("内部エラーが発生しました").color(ChatColor.RED).create());
            } else {
                foundName.accept(ret.get());
            }
        });
    }


}
