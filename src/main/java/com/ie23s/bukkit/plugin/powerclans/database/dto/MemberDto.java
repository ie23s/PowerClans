package com.ie23s.bukkit.plugin.powerclans.database.dto;

/**
 * Data transfer object for the {@code clan_members} table.
 *
 * @param clanUuid   UUID of the clan this member belongs to (FK → {@code clan_list.uuid})
 * @param name       player name (original case)
 * @param playerUuid Mojang UUID string of the player
 * @param isModer    {@code true} if the member has moderator privileges
 */
public record MemberDto(
        String clanUuid,
        String name,
        String playerUuid,
        boolean isModer
) {
}
