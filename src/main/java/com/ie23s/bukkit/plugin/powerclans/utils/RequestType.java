package com.ie23s.bukkit.plugin.powerclans.utils;

/**
 * Enumerates all pending-confirmation request types used by {@link Request}.
 */
public enum RequestType {
    /** Clan invitation — the target accepts to join the inviting clan. */
    INVITE,
    /** Clan creation — the initiator confirms paying the creation cost and founding the clan. */
    CREATE,
    /** Clan disbandment — the leader confirms dissolving the clan. */
    DISBAND,
    /** Clan leave — a member confirms their departure from the clan. */
    LEAVE,
    /** Leadership transfer — the leader confirms passing leadership to another member. */
    LEADER_TRANSFER,
    /** Clan upgrade — the leader confirms paying the upgrade cost to level up the clan. */
    UPGRADE
}
