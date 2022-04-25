package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.Variable;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayerTime;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public class BTLPAFKTagVariable extends Variable {

    private final BungeePlayTime owner;
    public static final String VAR_KEY = "bpt_afk_tag";

    public BTLPAFKTagVariable(BungeePlayTime owner) {
        super(VAR_KEY);
        this.owner = owner;
    }

    @Override
    public String getReplacement(ProxiedPlayer player) {
        Optional<PlayerTime> time = owner.getPlayer(player.getUniqueId());
        return (time.isPresent() && time.get().isAFK()) ? ChatColor.GRAY + "AFK" : "";
    }


    public static void unregisterFromBTLPVariable() throws RuntimeException {
        try {
            Field field = BungeeTabListPlusAPI.class.getDeclaredField("instance");
            field.setAccessible(true);
            BungeeTabListPlusAPI api = (BungeeTabListPlusAPI) field.get(null);

            field = api.getClass().getDeclaredField("variablesByName");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Variable> variablesByName = (Map<String, Variable>) field.get(api);

            field = api.getClass().getDeclaredField("btlp");
            field.setAccessible(true);
            BungeeTabListPlus btlp = (BungeeTabListPlus) field.get(api);


            if (variablesByName.remove(VAR_KEY) != null)
                btlp.scheduleSoftReload();

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

    }

}
