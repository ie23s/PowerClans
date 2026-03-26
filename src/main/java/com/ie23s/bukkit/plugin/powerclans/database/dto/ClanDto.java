package com.ie23s.bukkit.plugin.powerclans.database.dto;

import com.ie23s.bukkit.plugin.powerclans.api.IClan;

/**
 * Data transfer object for the {@code clan_list} table.
 *
 * @param id         auto-increment primary key (0 when not yet persisted)
 * @param uuid       stable CHAR(36) UUID used as FK in related tables
 * @param name       display name (case-preserved, unique)
 * @param leaderUuid Mojang UUID string of the current leader
 * @param maxPlayers maximum number of members allowed
 * @param level      clan level
 */
public record ClanDto(
        int id,
        String uuid,
        String name,
        String leaderUuid,
        int maxPlayers,
        int level
) {
    public static ClanDto from(IClan clan) {
        return new ClanDto(clan.getId(), clan.getUuid(), clan.getName(),
                clan.getLeaderUuid().toString(), clan.getMaxPlayers(), clan.getLevel());
    }
}
