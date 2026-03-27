package com.ie23s.bukkit.plugin.powerclans.clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * In-memory registry of all active {@link Member} objects.
 *
 * <p>Maintains two indexes:
 * <ul>
 *   <li><b>UUID index</b> — primary; used in event handlers where a {@link org.bukkit.entity.Player}
 *       object is available and its UUID can be obtained without any I/O.</li>
 *   <li><b>Name index</b> — secondary (lower-case key); used in command handlers where the
 *       input is a player name string (e.g. {@code args[1]}).</li>
 * </ul>
 */
public class MemberList {

    /** Primary index: player UUID → member. */
    private final HashMap<UUID, Member> byUuid = new HashMap<>();

    /** Secondary index: lower-case player name → member. */
    private final HashMap<String, Member> byName = new HashMap<>();

    // ── UUID-based (preferred for event handlers) ─────────────────────────────

    /**
     * Returns {@code true} if a member with the given UUID is registered.
     *
     * @param playerUuid Mojang UUID of the player
     * @return {@code true} if the player is a registered member
     */
    public boolean isMemberByUuid(UUID playerUuid) {
        return byUuid.containsKey(playerUuid);
    }

    /**
     * Returns the member with the given UUID, or {@code null} if not found.
     *
     * @param playerUuid Mojang UUID of the player
     * @return the matching {@link Member}, or {@code null}
     */
    public Member getMemberByUuid(UUID playerUuid) {
        return byUuid.get(playerUuid);
    }

    // ── Name-based (for command handlers that receive player names) ───────────

    /**
     * Returns {@code true} if a member with the given name is registered (case-insensitive).
     *
     * @param name player name (any case)
     * @return {@code true} if the player is a registered member
     */
    public boolean isMember(String name) {
        return byName.containsKey(name.toLowerCase());
    }

    /**
     * Returns the member with the given name, or {@code null} if not found (case-insensitive).
     *
     * @param name player name (any case)
     * @return the matching {@link Member}, or {@code null}
     */
    public Member getMember(String name) {
        return byName.get(name.toLowerCase());
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /**
     * Registers a member in both indexes.
     * If the member has no UUID yet (e.g. freshly migrated row with {@code null} UUID),
     * they are only stored in the name index until a UUID becomes available.
     *
     * @param member the member to register
     */
    public void addMember(Member member) {
        byName.put(member.getName().toLowerCase(), member);
        byUuid.put(member.getPlayerUuid(), member);
    }

    /**
     * Removes a member from both indexes, looked up by player UUID.
     *
     * @param playerUuid Mojang UUID of the player to remove
     */
    void removeMember(UUID playerUuid) {
        Member removed = byUuid.remove(playerUuid);
        if (removed != null) {
            byName.remove(removed.getName().toLowerCase());
        }
    }

    /**
     * Removes all members of the given clan from both indexes in a single pass.
     * Intended for clan disbanding, where every member row is deleted at once.
     *
     * @param clanName display name of the clan (case-insensitive)
     */
    void removeAllByClan(String clanName) {
        byName.values().removeIf(m -> {
            if (m.getClan().equalsIgnoreCase(clanName)) {
                byUuid.remove(m.getPlayerUuid());
                return true;
            }
            return false;
        });
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns all members of the given clan.
     *
     * @param clanName display name of the clan (case-insensitive)
     * @return mutable list of members; empty if none exist
     */
    public List<Member> getListOfMembers(String clanName) {
        List<Member> list = new ArrayList<>();
        for (Member member : byName.values()) {
            if (member.getClan().equalsIgnoreCase(clanName)) {
                list.add(member);
            }
        }
        return list;
    }
}
