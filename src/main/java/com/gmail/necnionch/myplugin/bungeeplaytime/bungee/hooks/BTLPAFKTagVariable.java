package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks;

import codecrafter47.bungeetablistplus.api.bungee.Variable;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayerTime;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

public class BTLPAFKTagVariable extends Variable {

    private final BungeePlayTime owner;

    public BTLPAFKTagVariable(BungeePlayTime owner) {
        super("bpt_afk_tag");
        this.owner = owner;
    }

    @Override
    public String getReplacement(ProxiedPlayer player) {
        Optional<PlayerTime> time = owner.getPlayer(player.getUniqueId());
        return (time.isPresent() && time.get().isAFK()) ? ChatColor.GRAY + "AFK" : "";
    }

}
