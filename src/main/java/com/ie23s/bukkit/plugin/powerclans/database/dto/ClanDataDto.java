package com.ie23s.bukkit.plugin.powerclans.database.dto;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.api.IClan;
import com.ie23s.bukkit.plugin.powerclans.api.IClanData;

/**
 * Data transfer object for the {@code clan_data} table.
 *
 * @param clanUuid    CHAR(36) UUID of the owning clan (PK and FK → {@code clan_list.uuid})
 * @param tag         colour-coded clan tag displayed in chat
 * @param home        serialised home location ({@code "world;x;y;z;yaw;pitch"}) or {@code "none"}
 * @param pvp         whether clan PvP mode is enabled
 * @param balance     clan bank balance
 * @param mobKills    cumulative mob-kill count
 * @param playerKills cumulative player-kill count
 * @param onlineTime  cumulative online time in minutes
 */
public record ClanDataDto(
        String clanUuid,
        String tag,
        String home,
        boolean pvp,
        double balance,
        int mobKills,
        int playerKills,
        int onlineTime
) {
    public static ClanDataDto from(IClan identity, IClanData data) {
        return new ClanDataDto(
                identity.getUuid(),
                data.getString(ClanDataKey.TAG),
                data.getString(ClanDataKey.HOME),
                data.getBoolean(ClanDataKey.PVP),
                data.getDouble(ClanDataKey.BALANCE),
                data.getInt(ClanDataKey.MOB_KILLS),
                data.getInt(ClanDataKey.PLAYER_KILLS),
                data.getInt(ClanDataKey.ONLINE_TIME)
        );
    }
}
