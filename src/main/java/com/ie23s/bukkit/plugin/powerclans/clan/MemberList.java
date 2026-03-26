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
     */
    public boolean isMemberByUuid(UUID playerUuid) {
        return byUuid.containsKey(playerUuid);
    }

    /**
     * Returns the member with the given UUID, or {@code null} if not found.
     *
     * @param playerUuid Mojang UUID of the player
     */
    public Member getMemberByUuid(UUID playerUuid) {
        return byUuid.get(playerUuid);
    }

    // ── Name-based (for command handlers that receive player names) ───────────

    /**
     * Returns {@code true} if a member with the given name is registered (case-insensitive).
     *
     * @param name player name (any case)
     */
    public boolean isMember(String name) {
        return byName.containsKey(name.toLowerCase());
    }

    /**
     * Returns the member with the given name, or {@code null} if not found (case-insensitive).
     *
     * @param name player name (any case)
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
     * Removes a member from both indexes, looked up by player name.
     *
     * @param name player name (any case)
     */
    void removeMember(String name) {
        Member removed = byName.remove(name.toLowerCase());
        if (removed != null) {
            byUuid.remove(removed.getPlayerUuid());
        }
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
