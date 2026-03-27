package com.ie23s.bukkit.plugin.powerclans.api;

import java.util.UUID;

/**
 * Core identity fields of a clan — corresponds to the {@code clan_list} table columns.
 */
public interface IClan {
    /**
     * Auto-increment database primary key.
     * @return the numeric row ID
     */
    int getId();

    /**
     * Unique identifier of the clan (UUID string).
     * @return the clan UUID as a string
     */
    String getUuid();

    /**
     * Display name of the clan (color codes stripped).
     * @return the clan name
     */
    String getName();

    /**
     * Mojang UUID of the current leader.
     * @return the leader's UUID
     */
    UUID getLeaderUuid();

    /**
     * Maximum number of members allowed in the clan.
     * @return the member capacity
     */
    int getMaxPlayers();

    /**
     * Current level of the clan.
     * @return the clan level
     */
    int getLevel();
}
