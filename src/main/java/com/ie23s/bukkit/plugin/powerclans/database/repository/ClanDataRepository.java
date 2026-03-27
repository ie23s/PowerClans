package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Data-access contract for the {@code clan_data} EAV table.
 *
 * <p>Each clan has one row per {@link ClanDataKey}; rows are identified by the
 * composite key {@code (clan_uuid, ident)}.
 */
public interface ClanDataRepository {

    /**
     * Returns all rows from {@code clan_data} across all clans.
     *
     * @return list of EAV DTOs; empty if none exist
     * @throws SQLException if a database error occurs
     */
    List<ClanDataDto> findAll() throws SQLException;

    /**
     * Inserts one row per entry in {@code data} for the given clan.
     * Intended for initial creation of a new clan's data block.
     *
     * @param clanUuid UUID of the clan
     * @param data     map of keys to their current in-memory values
     * @throws SQLException if a database error occurs
     */
    void insertAll(String clanUuid, Map<ClanDataKey, Object> data) throws SQLException;

    /**
     * Updates an existing row, or inserts it if it does not yet exist.
     *
     * @param clanUuid UUID of the clan
     * @param ident    key identifier (see {@link ClanDataKey#ident()})
     * @param value    string-serialised new value
     * @throws SQLException if a database error occurs
     */
    void upsert(String clanUuid, String ident, String value) throws SQLException;

    /**
     * Deletes the row for the given clan and key, if it exists.
     * No-op if the row does not exist.
     *
     * @param clanUuid UUID of the clan
     * @param ident    key identifier (see {@link ClanDataKey#ident()})
     * @throws SQLException if a database error occurs
     */
    void delete(String clanUuid, String ident) throws SQLException;
}
