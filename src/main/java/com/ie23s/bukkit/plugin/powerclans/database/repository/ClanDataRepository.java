package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;

import java.sql.SQLException;
import java.util.List;

/**
 * Data-access contract for the {@code clan_data} table.
 * Covers secondary/mutable clan attributes: tag, home, pvp, balance, and stat counters.
 */
public interface ClanDataRepository {

    /**
     * Returns secondary data for all clans.
     *
     * @return list of clan data DTOs; empty if none exist
     */
    List<ClanDataDto> findAll() throws SQLException;

    /**
     * Inserts a new row into {@code clan_data}.
     *
     * @param data DTO with all secondary fields
     */
    void insert(ClanDataDto data) throws SQLException;

    /** Updates the balance for the given clan. */
    void updateBalance(String clanUuid, double balance) throws SQLException;

    /** Updates the PvP flag for the given clan. */
    void updatePvp(String clanUuid, boolean pvp) throws SQLException;

    /** Updates the home location string for the given clan. */
    void updateHome(String clanUuid, String home) throws SQLException;

    /** Updates the mob-kill counter for the given clan. */
    void updateMobKills(String clanUuid, int mobKills) throws SQLException;

    /** Updates the player-kill counter for the given clan. */
    void updatePlayerKills(String clanUuid, int playerKills) throws SQLException;

    /** Updates the cumulative online-time counter for the given clan. */
    void updateOnlineTime(String clanUuid, int onlineTime) throws SQLException;
}
