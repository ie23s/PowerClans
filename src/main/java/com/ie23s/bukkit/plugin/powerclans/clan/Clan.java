package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.api.IClan;
import com.ie23s.bukkit.plugin.powerclans.api.IClanData;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory representation of a clan.
 *
 * <p>Combines identity fields (from {@code clan_list}) with mutable secondary data
 * (from {@code clan_data}) into a single object. Identity fields are immutable after
 * construction; secondary data is stored in an {@link EnumMap} keyed by {@link ClanDataKey}
 * and updated via {@link #set(ClanDataKey, Object)}.
 *
 * <p>Mutators such as {@link #setLeader} update the in-memory state and dispatch persistence
 * calls asynchronously via the Bukkit scheduler.
 *
 * <p><b>Member management is intentionally absent.</b> Operations that add, remove, or
 * change the role of members are the responsibility of {@link ClanList}, which owns both
 * the clan registry and the member lifecycle.
 */
public class Clan implements IClan, IClanData {

    private final Core core;

    // clan_list fields
    private final int id;
    private final String uuid;
    private final String name;
    private UUID leaderUuid;
    private int maxPlayers;
    private int level;

    // clan_data fields
    private final Map<ClanDataKey, Object> data = new EnumMap<>(ClanDataKey.class);

    /**
     * Constructs a clan with all identity fields.
     * Secondary data fields are initialised to their {@link ClanDataKey#defaultValue() defaults}.
     *
     * @param core       plugin core used to reach other registries and services
     * @param id         auto-increment database primary key ({@code 0} for clans not yet persisted)
     * @param uuid       stable UUID used as the FK in related tables
     * @param name       display name (case-preserved, unique)
     * @param leaderUuid Mojang UUID of the current leader
     * @param maxPlayers maximum member capacity
     * @param level      clan level
     */
    public Clan(Core core, int id, String uuid, String name, UUID leaderUuid, int maxPlayers, int level) {
        this.core = core;
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.leaderUuid = leaderUuid;
        this.maxPlayers = maxPlayers;
        this.level = level;
        for (ClanDataKey key : ClanDataKey.values()) {
            data.put(key, key.defaultValue());
        }
    }

    // ── IClan ─────────────────────────────────────────────────────────────────

    @Override public int    getId()         { return id; }
    @Override public String getUuid()       { return uuid; }
    @Override public String getName()       { return name; }
    @Override public UUID   getLeaderUuid() { return leaderUuid; }
    @Override public int    getMaxPlayers() { return maxPlayers; }
    @Override public int    getLevel()      { return level; }

    /**
     * Returns the current leader's player name, resolved from {@link MemberList}.
     *
     * @return the leader's player name
     */
    public String getLeaderName() {
        return core.getMemberList().getMemberByUuid(leaderUuid).getName();
    }

    // ── IClanData ─────────────────────────────────────────────────────────────

    @Override
    public Object getRaw(ClanDataKey key) {
        return data.getOrDefault(key, key.defaultValue());
    }

    @Override
    public void set(ClanDataKey key, Object value) {
        data.put(key, value);
    }

    // ── Own-data mutators (update in-memory + persist) ────────────────────────

    /**
     * Updates a clan data field in memory and persists it asynchronously.
     *
     * @param key   the field to update
     * @param value the new value
     */
    public void update(ClanDataKey key, Object value) {
        set(key, value);
        core.getClanDataService().update(this, key);
    }

    /**
     * Removes a clan data field from memory and deletes the row asynchronously.
     *
     * @param key the field to delete
     */
    public void delete(ClanDataKey key) {
        set(key, null);
        core.getClanDataService().delete(this, key);
    }

    /**
     * Changes the clan leader and persists the change asynchronously.
     *
     * @param leaderUuid UUID of the new leader
     */
    public void setLeader(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
        core.getClanService().updateLeader(this);
    }

    /**
     * Increases {@code max_players} by {@code i} and persists asynchronously.
     *
     * @param i number of slots to add
     */
    public void upgrade(int i) {
        this.maxPlayers += i;
        core.getClanService().updateMaxPlayers(this);
    }

    /** Increments the clan level by one and persists the change asynchronously. */
    public void addLevel() {
        ++this.level;
        core.getClanService().updateLevel(this);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the given UUID belongs to the current clan leader.
     *
     * @param playerUuid UUID to check
     * @return {@code true} if the UUID matches the leader
     */
    public boolean hasLeader(UUID playerUuid) {
        return leaderUuid.equals(playerUuid);
    }

    /**
     * Returns {@code true} if the member identified by the given UUID is a moderator
     * of this clan.
     *
     * @param playerUuid Mojang UUID of the player to check
     * @return {@code true} if the player is a moderator of this clan
     */
    public boolean hasModer(UUID playerUuid) {
        Member member = core.getMemberList().getMemberByUuid(playerUuid);
        return member != null && member.isModer() && member.getClan().equals(this.name);
    }

    /**
     * Returns {@code true} if the member identified by the given UUID belongs to this clan.
     *
     * @param playerUuid Mojang UUID of the player to check
     * @return {@code true} if the player is a member of this clan
     */
    public boolean hasClanMember(UUID playerUuid) {
        Member member = core.getMemberList().getMemberByUuid(playerUuid);
        return member != null && member.getClan().equals(this.name);
    }
}
