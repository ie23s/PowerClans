package com.ie23s.bukkit.plugin.powerclans.database.dto;

/**
 * Data transfer object for one row in the {@code clan_data} EAV table.
 *
 * <p>Each clan has one row per {@link com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey}.
 * All values are stored as strings and deserialised back to the correct type via
 * {@link com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey#parse(String)}.
 *
 * @param clanUuid CHAR(36) UUID of the owning clan (FK → {@code clan_list.uuid})
 * @param ident    key identifier matching {@link com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey#ident()}
 * @param value    string-serialised value
 */
public record ClanDataDto(String clanUuid, String ident, String value) {
}
