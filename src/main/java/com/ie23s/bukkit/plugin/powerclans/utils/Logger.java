package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    private final Core core;

    public Logger(Core core) {
        this.core = core;
    }

    public void info(Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[" + core.getName() + "] " + text);
    }

    @SuppressWarnings("unused")
    public void warning(Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + core.getName() + "] " + text);
    }

    public void error(Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + core.getName() + "] " + text);
    }

    public void debug(Object text) {
        if (core.getConfig().getBoolean("debug")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + core.getName() + "] [Debug] " + text);
        }
    }
}
