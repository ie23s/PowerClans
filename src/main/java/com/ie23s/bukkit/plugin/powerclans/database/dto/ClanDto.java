package com.ie23s.bukkit.plugin.powerclans.database.dto;

import com.ie23s.bukkit.plugin.powerclans.api.IClan;

/**
 * Data transfer object for the {@code clan_list} table.
 */
public record ClanDto(
        String uuid,
        String name,
        String leader,
        int maxPlayers,
        int level
) {
    public static ClanDto from(IClan clan) {
        return new ClanDto(clan.getUuid(), clan.getName(), clan.getLeader(),
                clan.getMaxPlayers(), clan.getLevel());
    }
}
