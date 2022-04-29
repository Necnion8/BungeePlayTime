package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PluginHook {
    protected final String pluginName;
    protected final Plugin owner;
    protected boolean enable;

    public PluginHook(Plugin owner, String pluginName) {
        this.owner = owner;
        this.pluginName = pluginName;
    }

    final public boolean hook() {
        try {
            Plugin plugin = owner.getServer().getPluginManager().getPlugin(pluginName);
            enable = this.onHook(plugin);
        } catch (Throwable e) {
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                owner.getLogger().log(Level.WARNING, "Failed to hook to " + pluginName + ": " + e.getMessage());
            } else {
                owner.getLogger().log(Level.WARNING, "Failed to hook to " + pluginName, e);
            }
            enable = false;
        }
        return enable;
    }

    final public void unhook() {
        if (!enable)
            return;
        enable = false;
        try {
            this.onUnhook();
        } catch (Throwable e) {
            owner.getLogger().log(Level.WARNING, "Failed to unhook to " + pluginName, e);

        }
    }

    protected abstract boolean onHook(Plugin plugin) throws Exception;

    protected abstract void onUnhook();


    public boolean isEnabled() {
        return enable;
    }

    public String getPluginName() {
        return pluginName;
    }

    public Logger getLogger() {
        return owner.getLogger();
    }

}
