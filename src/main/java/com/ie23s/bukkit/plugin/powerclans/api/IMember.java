package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Read contract for a clan member — corresponds to the {@code clan_members} table columns.
 */
public interface IMember {
    /** Player name of the member (always lowercase). */
    String getName();

    /** Whether the member has moderator privileges within their clan. */
    boolean isModer();

    /** Name of the clan this member belongs to. */
    String getClan();
}
