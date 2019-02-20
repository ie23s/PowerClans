package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    public static void info(Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[" + Main.plugin.getName() + "] " + text);
    }

    @SuppressWarnings("unused")
    public static void warning(Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + Main.plugin.getName() + "] " + text);
    }

    public static void error(Object text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + Main.plugin.getName() + "] " + text);
    }

    public static void debug(Object text) {
        if (Configuration.getConfiguration().getBoolean("debug")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + Main.plugin.getName() + "] [Debug] " + text);
        }
    }
}
