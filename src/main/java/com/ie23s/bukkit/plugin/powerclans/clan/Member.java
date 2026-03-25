package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.api.IMember;

import java.util.UUID;

/**
 * In-memory representation of a single clan member.
 *
 * <p>Player names are stored with their original casing as returned by Bukkit.
 * Case-insensitive lookups are the responsibility of {@link MemberList}.
 */
public class Member implements IMember {

    private final String name;
    private final UUID playerUuid;
    private boolean isModer;
    private final String clan;

    /**
     * @param name       player name (original case, not lowercased)
     * @param playerUuid Mojang UUID of the player
     * @param isModer    {@code true} if the member has moderator privileges
     * @param clan       display name of the clan this member belongs to
     */
    public Member(String name, UUID playerUuid, boolean isModer, String clan) {
        this.name = name;
        this.playerUuid = playerUuid;
        this.isModer = isModer;
        this.clan = clan;
    }

    @Override public String  getName()       { return name; }
    @Override public boolean isModer()       { return isModer; }
    @Override public String  getClan()       { return clan; }

    /** @return the player's Mojang UUID */
    public UUID getPlayerUuid() { return playerUuid; }

    void setModer(boolean isModer) { this.isModer = isModer; }
}
