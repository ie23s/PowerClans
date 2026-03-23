package com.ie23s.bukkit.plugin.powerclans.database.dto;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.api.IClan;
import com.ie23s.bukkit.plugin.powerclans.api.IClanData;

/**
 * Data transfer object for the {@code clan_data} table.
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
