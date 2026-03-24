package com.ie23s.bukkit.plugin.powerclans.database.dto;

/**
 * Data transfer object for the {@code clan_members} table.
 *
 * @param clanUuid CHAR(36) UUID of the clan this member belongs to (FK → {@code clan_list.uuid})
 * @param name     lower-case player name
 * @param isModer  {@code true} if the member has moderator privileges
 */
public record MemberDto(
        String clanUuid,
        String name,
        boolean isModer
) {
}
