package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.ChildCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandBungee;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.RootCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.errors.CommandError;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.errors.NotFoundCommandError;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.LookupTimeOptions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        addCommand("info", null, this::execInfo, this::compPlayers);
    }

    public void registerCommand() {
        Command command = CommandBungee.build(this, "bungeeplaytime", "bungeeplaytime.command.bungeeplaytime", "bpt");
        plugin.getProxy().getPluginManager().registerCommand(plugin, command);
    }


    @Override
    public void onError(@NotNull CommandSender sender, com.gmail.necnionch.myplugin.bungeeplaytime.common.command.@Nullable Command command, @NotNull CommandError error) {
        if (error instanceof NotFoundCommandError) {
            sender.sendMessage(new ComponentBuilder("[").color(ChatColor.GRAY).append("BungeePlayTime").color(ChatColor.AQUA).append("]").color(ChatColor.GRAY)
                    .append(" Commands").color(ChatColor.WHITE)
                    .append("\n/bpt ").color(ChatColor.GRAY).append("info ").color(ChatColor.WHITE).append("[player/uuid]").color(ChatColor.GRAY)
                    .append("\n/bpt ").color(ChatColor.GRAY).append("reload").color(ChatColor.WHITE)
                    .append("\n/bpt ").color(ChatColor.GRAY).append("debug ").color(ChatColor.WHITE).append("actives")
                    .append("\n/bpt ").color(ChatColor.GRAY).append("debug ").color(ChatColor.WHITE).append("brdigeRetry")
                    .create());
            return;
        }
        super.onError(sender, command, error);
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
        }, 3250, TimeUnit.MILLISECONDS);
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

        Consumer<PlayerName> foundName = (playerName) -> {
            CompletableFuture<OptionalLong> firstTimeFuture = plugin.lookupFirstTime(playerName.getUniqueId());
            CompletableFuture<OptionalLong> lastTimeFuture = plugin.lookupLastTime(playerName.getUniqueId());
            CompletableFuture<Optional<PlayerTimeResult>> timeFuture = plugin.lookupTime(playerName.getUniqueId());
            CompletableFuture<OptionalInt> rankingFuture = plugin.lookupTimeRanking(playerName.getUniqueId(), new LookupTimeOptions().totalTime(false));

            CompletableFuture.allOf(firstTimeFuture, lastTimeFuture, timeFuture, rankingFuture).whenComplete((ret, err) -> {
                b.append("[").color(ChatColor.GRAY).append("BungeePlayTime").color(ChatColor.AQUA).append("] ").color(ChatColor.GRAY);
                b.append("Info").color(ChatColor.WHITE).append(" - ").color(ChatColor.GRAY);
                b.append(playerName.getName()).color(ChatColor.GOLD);
                b.append("  (" + playerName.getUniqueId() + ")").color(ChatColor.GRAY);

                b.append("\n初ログイン: ").color(ChatColor.GOLD);
                if (firstTimeFuture.isCompletedExceptionally()) {
                    b.append("エラー").color(ChatColor.DARK_RED);
                } else {
                    OptionalLong value = firstTimeFuture.getNow(OptionalLong.empty());
                    if (value.isPresent()) {
                        b.append(BPTUtil.formatEpochTime(value.getAsLong())).color(ChatColor.WHITE);
                    } else {
                        b.append("不明").color(ChatColor.GRAY);
                    }
                }

                b.append("\n最終ログイン: ").color(ChatColor.GOLD);
                if (lastTimeFuture.isCompletedExceptionally()) {
                    b.append("エラー").color(ChatColor.DARK_RED);
                } else {
                    OptionalLong value = lastTimeFuture.getNow(OptionalLong.empty());
                    if (value.isPresent()) {
                        b.append(BPTUtil.formatEpochTime(value.getAsLong())).color(ChatColor.WHITE);
                    } else {
                        b.append("不明").color(ChatColor.GRAY);
                    }
                }

                Optional<PlayerTimeResult> playerTime = Optional.empty();
                b.append("\nプレイ時間: ").color(ChatColor.GOLD);
                if (timeFuture.isCompletedExceptionally()) {
                    b.append("エラー").color(ChatColor.DARK_RED);
                } else {
                    playerTime = timeFuture.getNow(Optional.empty());
                    if (playerTime.isPresent()) {
                        if (!rankingFuture.isCompletedExceptionally()) {
                            OptionalInt value2 = rankingFuture.getNow(OptionalInt.empty());
                            if (value2.isPresent()) {
                                b.append("(#" + (value2.getAsInt()+1) + ")  ").color(ChatColor.YELLOW);
                            }
                        }
                        PlayerTimeResult time = playerTime.get();
                        b.append(BPTUtil.formatTimeText(time.getPlayTime())).color(ChatColor.WHITE);
                        double playDiv = (double) time.getPlayTime() / time.getTotalTime();
                        b.append(" (" + Math.round(playDiv*100) + "%)").color(ChatColor.GRAY);
                    } else {
                        b.append("不明").color(ChatColor.GRAY);
                    }
                }
                playerTime.ifPresent(playerTimeResult -> {
                    b.append("\n接続時間: ").color(ChatColor.GOLD);
                    b.append(BPTUtil.formatTimeText(playerTimeResult.getTotalTime())).color(ChatColor.GRAY);
                });

                s.sendMessage(b.create());
            });
        };

        if (args.isEmpty()) {
            if (s.getSender() instanceof ProxiedPlayer) {
                ProxiedPlayer p = (ProxiedPlayer) s.getSender();
                foundName.accept(new PlayerName(p.getUniqueId(), p.getName()));
                return;
            }
            s.sendMessage(b.append("プレイヤーを指定してください").color(ChatColor.RED).create());
            return;
        }

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

    @NotNull
    private List<String> compPlayers(CommandSender s, String l, List<String> args) {
        if (args.size() == 1)
            return generateSuggests(args.get(0), plugin.getPlayerNameCache().values()
                    .stream()
                    .sorted(Comparator.naturalOrder())
                    .sorted((n1, n2) -> {
                        ProxiedPlayer p1 = plugin.getProxy().getPlayer(n1);
                        ProxiedPlayer p2 = plugin.getProxy().getPlayer(n2);
                        if ((p1 != null && p2 != null) || (p1 == null && p2 == null))
                            return 0;
                        if (p1 != null)
                            return -1;
                        return 1;
                    })
                    .distinct()
                    .toArray(String[]::new));
        return Collections.emptyList();
    }


}
