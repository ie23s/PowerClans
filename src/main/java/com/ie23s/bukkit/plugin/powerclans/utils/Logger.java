package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Level;

/**
 * Plugin-scoped console logger.
 *
 * <p>Simple messages are sent via {@link org.bukkit.command.ConsoleCommandSender} with colour
 * prefixes. Exceptions that carry a {@link Throwable} are forwarded to the plugin's
 * {@link java.util.logging.Logger} so the full stack trace appears in the server log.
 */
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

    /**
     * Logs an error message together with the full stack trace of the given throwable.
     *
     * @param message human-readable description of the failure
     * @param throwable the exception whose stack trace should be recorded
     */
    public void error(String message, Throwable throwable) {
        core.getLogger().log(Level.SEVERE, message, throwable);
    }

    public void debug(Object text) {
        if (core.getConfig().getBoolean("debug")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + core.getName() + "] [Debug] " + text);
        }
    }
}
