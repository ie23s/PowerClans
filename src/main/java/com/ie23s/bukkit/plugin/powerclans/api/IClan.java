package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Core identity fields of a clan — corresponds to the {@code clan_list} table columns.
 */
public interface IClan {
    String getUuid();
    String getName();
    String getLeader();
    int getMaxPlayers();
    int getLevel();
}
