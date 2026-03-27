package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;

import java.sql.SQLException;
import java.util.List;

/**
 * Data-access contract for the {@code clan_members} table.
 */
public interface MemberRepository {

    /**
     * Returns all clan members stored in the database.
     *
     * @return list of member DTOs; empty if none exist
     * @throws SQLException if a database error occurs
     */
    List<MemberDto> findAll() throws SQLException;

    /**
     * Inserts a new member row with {@code isModer} defaulting to {@code false}.
     *
     * @param member DTO with clan uuid, player name, and moderator flag
     * @throws SQLException if a database error occurs
     */
    void insert(MemberDto member) throws SQLException;

    /**
     * Updates the moderator status of a specific member.
     *
     * @param clanUuid   uuid of the clan the member belongs to
     * @param memberName player name of the member
     * @param isModer    new moderator status
     * @throws SQLException if a database error occurs
     */
    void updateModer(String clanUuid, String memberName, boolean isModer) throws SQLException;

    /**
     * Removes a single member from their clan (kick).
     *
     * @param clanUuid   uuid of the clan the member belongs to
     * @param memberName player name of the member to remove
     * @throws SQLException if a database error occurs
     */
    void delete(String clanUuid, String memberName) throws SQLException;

    /**
     * Removes all members of a clan (used during disband).
     *
     * @param clanUuid uuid of the disbanded clan
     * @throws SQLException if a database error occurs
     */
    void deleteByClan(String clanUuid) throws SQLException;

    /**
     * Sets the {@code player_uuid} column for an existing member row.
     * Used by the V3 migration Java step to back-fill UUIDs for existing members.
     *
     * @param clanUuid   clan uuid of the member row
     * @param memberName player name identifying the row
     * @param playerUuid Mojang UUID string to store
     * @throws SQLException if a database error occurs
     */
    void updatePlayerUuid(String clanUuid, String memberName, String playerUuid) throws SQLException;
}
