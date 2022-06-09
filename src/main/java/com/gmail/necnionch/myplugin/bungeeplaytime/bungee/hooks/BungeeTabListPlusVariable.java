package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;

public class BungeeTabListPlusVariable {

    public static BungeeTabListPlusVariable register(BungeePlayTime owner) {
        BungeeTabListPlusVariable var = new BungeeTabListPlusVariable();
        BungeeTabListPlusAPI.registerVariable(owner, new BTLPAFKTagVariable(owner));
        return var;
    }

    public void unregister() {
        BTLPAFKTagVariable.unregisterFromBTLPVariable();
    }
}
