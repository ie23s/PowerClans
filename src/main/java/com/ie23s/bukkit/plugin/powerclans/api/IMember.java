package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Read contract for a clan member — corresponds to the {@code clan_members} table columns.
 */
public interface IMember {
    String getName();
    boolean isModer();
    String getClan();
}
