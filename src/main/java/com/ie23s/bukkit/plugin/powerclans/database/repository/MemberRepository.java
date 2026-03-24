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
     */
    List<MemberDto> findAll() throws SQLException;

    /**
     * Inserts a new member row with {@code isModer} defaulting to {@code false}.
     *
     * @param member DTO with clan uuid, player name, and moderator flag
     */
    void insert(MemberDto member) throws SQLException;

    /**
     * Updates the moderator status of a specific member.
     *
     * @param clanUuid   uuid of the clan the member belongs to
     * @param memberName player name of the member
     * @param isModer    new moderator status
     */
    void updateModer(String clanUuid, String memberName, boolean isModer) throws SQLException;

    /**
     * Removes a single member from their clan (kick).
     *
     * @param clanUuid   uuid of the clan the member belongs to
     * @param memberName player name of the member to remove
     */
    void delete(String clanUuid, String memberName) throws SQLException;

    /**
     * Removes all members of a clan (used during disband).
     *
     * @param clanUuid uuid of the disbanded clan
     */
    void deleteByClan(String clanUuid) throws SQLException;
}
