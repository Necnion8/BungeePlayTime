# BungeePlayTime
プレイヤーのオンライン時間とプレイ時間を集計するプラグイン

## 前提
- BungeeCord
- Spigot (AFK連携を使用する場合)
- 連携プラグイン (オプション)
    - [AFKPlus](https://www.spigotmc.org/resources/afk.35065/) (AFK判定の連携)
    - [ConnectorPlugin](https://github.com/Phoenix616/ConnectorPlugin) (ブリッジ通信)
    - [BungeeTabListPlus](https://www.spigotmc.org/resources/bungeetablistplus.313/) (プレースホルダ連携)

## コマンドと権限

| コマンド                          | サブコマンド             | 説明            | 権限                                    | デフォルト<sup>※1</sup> | 短縮名             |
|:------------------------------|--------------------|:--------------|:--------------------------------------|:------------------:|:----------------|
| /playtime [player]            |                    | 実行者のプレイ時間     | bungeeplaytime.command.playtime       |        YES         | /pt             |
| /playtimetop                  |                    | プレイ時間ランキング    | bungeeplaytime.command.playtimetop    |        OPのみ        | /pttop, /pt top |
| /onlinetime [player]          |                    | 実行者のオンライン時間   | bungeeplaytime.command.onlinetime     |        YES         | /ot             |
| /onlinetimetop                |                    | オンライン時間ランキング  | bungeeplaytime.command.onlinetimetop  |        OPのみ        | /ottop, /ot top |
| /afkplayers <sup>※1</sup>     |                    | AFKプレイヤー一覧    | bungeeplaytime.command.afkplayers     |                    |                 |
| /bungeeplaytime <sup>※1</sup> |                    | 管理者用プラグインコマンド | bungeeplaytime.command.bungeeplaytime |                    | /bpt            |
|                               | info [player/uuid] | プレイヤーの情報を表示   |                                       |                    |                 |
|                               | reload             | 設定の再読み込み      |                                       |                    |
<sup>※1</sup> BungeeCordコマンドのみ

## 設定
[BungeeCord config.yml](..%2Fsrc%2Fmain%2Fresources%2Fbungee-config.yml) | [Bukkit config.yml](..%2Fsrc%2Fmain%2Fresources%2Fbukkit-config.yml)
```yml
### BungeeCord config.yml ###

players:
  played-in-unknown-state: false
  afk-minutes: 5

db-type: mysql
# available: mysql, sqlite

database:
  mysql:
    username: root
    password: "password"
    address: localhost:3306
    database: "bungeeplaytime"
    options:
      autoReconnect: true

  sqlite:
    address: database.db
    options:

# Use ConnectorPlugin in API communication
enable-connector-plugin-support: true
```

> [!NOTE]
> データベースに MySQL か SQLite を使用できますが、デフォルトは MySQL になっています。<br>
> 必要に応じて変更してください。`db-type: mysql`


## API
- Bungee/Bukkit 共通メソッド - [common/IPlayTimeAPI.java](..%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgmail%2Fnecnionch%2Fmyplugin%2Fbungeeplaytime%2Fcommon%2FIPlayTimeAPI.java)<br>
- Bukkit - [bukkit/PlayTimeAPI.java](..%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgmail%2Fnecnionch%2Fmyplugin%2Fbungeeplaytime%2Fbukkit%2FPlayTimeAPI.java)<br>
- Bungee - [bungee/PlayTimeAPI.java](..%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgmail%2Fnecnionch%2Fmyplugin%2Fbungeeplaytime%2Fbungee%2FPlayTimeAPI.java)

### 例: プレイ時間ランクの取得
> ```java
> Player player = Bukkit.getPlayer("Necnion8");
>
> if (Bukkit.getPluginManager().isPluginEnabled("BungeePlayTime")) {
>   PlayTimeAPI api = BungeePlayTime.getAPI();
>     
>   api.lookupTimeRanking(player.getUniqueId(), new LookupTimeOptions().server("game"))
>     .thenAccept(ret -> {
>       if (ret.isPresent()) {
>         getLogger().info("Player " + player.getName() + "'s rank " + ret.getAsInt());
>       } else {
>         getLogger().info("No data player");
>       }
>     });
> }
> ```
