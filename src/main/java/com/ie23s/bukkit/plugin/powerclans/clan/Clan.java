package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.api.IClan;
import com.ie23s.bukkit.plugin.powerclans.api.IClanData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
 * <p>Mutator methods (e.g. {@link #setLeader}, {@link #setModer(String, boolean)}) update the in-memory
 * state immediately and dispatch a persistence call through the relevant service
 * asynchronously via the Bukkit scheduler.
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

    // ── Mutators (update in-memory + persist) ─────────────────────────────────

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

    // ── Member management ─────────────────────────────────────────────────────

    /**
     * Adds an online player to this clan in memory and persists the row asynchronously.
     *
     * @param player the online player joining this clan
     */
    public void invite(Player player) {
        Member member = new Member(player.getName(), player.getUniqueId(), false, this.name);
        core.getMemberList().addMember(member);
        core.getMemberService().create(member);
    }

    /**
     * Removes the player from this clan in memory and deletes the row asynchronously.
     *
     * @param name player name to remove
     */
    public void kick(String name) {
        core.getMemberService().delete(core.getMemberList().getMember(name));
        core.getMemberList().removeMember(name);
    }

    /**
     * Updates the moderator flag of a clan member in memory and persists asynchronously.
     *
     * @param name    player name of the member
     * @param isModer {@code true} to promote, {@code false} to demote
     */
    public void setModer(String name, boolean isModer) {
        Member member = core.getMemberList().getMember(name);
        member.setModer(isModer);
        core.getMemberService().updateModer(member);
    }

    /**
     * Returns {@code true} if the given player is a moderator of this clan.
     *
     * @param name player name to check
     * @return {@code true} if the player is a moderator of this clan
     */
    public boolean hasModer(String name) {
        Member member = core.getMemberList().getMember(name);
        return member.isModer() && member.getClan().equals(this.name);
    }

    /**
     * Kicks all members, removes the clan from {@link com.ie23s.bukkit.plugin.powerclans.clan.ClanList},
     * and deletes the clan and all its members from the database asynchronously.
     */
    public void disband() {
        for (Member mem : core.getMemberList().getListOfMembers(this.name))
            kick(mem.getName());
        core.getClanList().getClans().remove(this.name.toLowerCase());
        core.getMemberService().deleteByClan(this.name);
        core.getClanService().delete(this);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if {@code player} is the leader of this clan (case-insensitive).
     *
     * @param player player name to check
     * @return {@code true} if the player is the clan leader
     */
    public boolean hasLeader(String player) {
        return getLeaderName().equalsIgnoreCase(player);
    }

    /**
     * Returns {@code true} if the given UUID matches the current leader.
     *
     * @param uuid UUID to check
     * @return {@code true} if the UUID belongs to the clan leader
     */
    public boolean hasLeader(UUID uuid) {
        return leaderUuid.equals(uuid);
    }

    /**
     * Returns {@code true} if {@code name} is a member of this clan.
     * Use in command handlers where only a player name is available.
     *
     * @param name player name to check
     * @return {@code true} if the player is a member of this clan
     */
    public boolean hasClanMember(String name) {
        Member member = core.getMemberList().getMember(name);
        return member != null && member.getClan().equals(this.name);
    }

    /**
     * Returns {@code true} if the given online player is a member of this clan.
     * Prefer this overload in event handlers where a {@link Player} object is available.
     *
     * @param player the online player to check
     * @return {@code true} if the player is a member of this clan
     */
    public boolean hasClanMember(Player player) {
        Member member = core.getMemberList().getMemberByUuid(player.getUniqueId());
        return member != null && member.getClan().equals(this.name);
    }

    /**
     * Sends a formatted message to all online clan members.
     *
     * @param message message text to broadcast
     */
    public void broadcast(String message) {
        for (Member member : core.getMemberList().getListOfMembers(this.name)) {
            Player pl = Bukkit.getPlayer(member.getPlayerUuid());
            if (pl != null) {
                pl.sendMessage(core.lang("command.broadcast_format", core.lang("chat.clan"), message));
            }
        }
    }
}
