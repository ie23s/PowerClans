package com.ie23s.bukkit.plugin.powerclans.api;

import java.util.UUID;

/**
 * Core identity fields of a clan — corresponds to the {@code clan_list} table columns.
 */
public interface IClan {
    /** Auto-increment database primary key. */
    int getId();

    /** Unique identifier of the clan (UUID string). */
    String getUuid();

    /** Display name of the clan (color codes stripped). */
    String getName();

    /** Mojang UUID of the current leader. */
    UUID getLeaderUuid();

    /** Maximum number of members allowed in the clan. */
    int getMaxPlayers();

    /** Current level of the clan. */
    int getLevel();
}
