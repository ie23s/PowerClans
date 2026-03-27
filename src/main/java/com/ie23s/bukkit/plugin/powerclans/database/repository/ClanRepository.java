package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;

import java.sql.SQLException;
import java.util.List;

/**
 * Data-access contract for the {@code clan_list} table.
 * Covers core clan identity: uuid, name, leader, max_players, level.
 */
public interface ClanRepository {

    /**
     * Returns all clans stored in the database.
     *
     * @return list of clan DTOs; empty if none exist
     * @throws SQLException if a database error occurs
     */
    List<ClanDto> findAll() throws SQLException;

    /**
     * Inserts a new clan row.
     *
     * @param clan DTO with uuid, name, leader, maxPlayers, level
     * @throws SQLException if a database error occurs
     */
    void insert(ClanDto clan) throws SQLException;

    /**
     * Updates the leader UUID of the given clan.
     *
     * @param clanUuid   UUID of the clan
     * @param leaderUuid new leader's Mojang UUID string
     * @throws SQLException if a database error occurs
     */
    void updateLeader(String clanUuid, String leaderUuid) throws SQLException;

    /**
     * Updates the maximum member count for the given clan.
     *
     * @param clanUuid   UUID of the clan
     * @param maxPlayers new maximum
     * @throws SQLException if a database error occurs
     */
    void updateMaxPlayers(String clanUuid, int maxPlayers) throws SQLException;

    /**
     * Updates the level for the given clan.
     *
     * @param clanUuid UUID of the clan
     * @param level    new level
     * @throws SQLException if a database error occurs
     */
    void updateLevel(String clanUuid, int level) throws SQLException;

    /**
     * Deletes the clan row. Does not cascade to members — call
     * {@link MemberRepository#deleteByClan(String)} separately.
     *
     * @param clanUuid UUID of the clan to delete
     * @throws SQLException if a database error occurs
     */
    void delete(String clanUuid) throws SQLException;
}
