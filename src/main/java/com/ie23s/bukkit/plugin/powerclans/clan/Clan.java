package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.api.IClan;
import com.ie23s.bukkit.plugin.powerclans.api.IClanData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory representation of a clan.
 *
 * <p>Combines identity fields (from {@code clan_list}) with mutable secondary data
 * (from {@code clan_data}) into a single object. Identity fields are immutable after
 * construction; secondary data is stored in an {@link EnumMap} keyed by {@link ClanDataKey}
 * and updated via {@link #set(ClanDataKey, Object)}.
 *
 * <p>Mutator methods (e.g. {@link #setLeader}, {@link #setBalance}) update the in-memory
 * state immediately and dispatch a persistence call through the relevant service
 * asynchronously via the Bukkit scheduler.
 */
public class Clan implements IClan, IClanData {

    private final Core core;

    // clan_list fields
    private final int id;
    private final String uuid;
    private final String name;
    private String leader;
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
     * @param leader     player name of the current leader (lower-case)
     * @param maxPlayers maximum member capacity
     * @param level      clan level
     */
    public Clan(Core core, int id, String uuid, String name, String leader, int maxPlayers, int level) {
        this.core = core;
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.leader = leader;
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
    @Override public String getLeader()     { return leader; }
    @Override public int    getMaxPlayers() { return maxPlayers; }
    @Override public int    getLevel()      { return level; }

    // ── IClanData ─────────────────────────────────────────────────────────────

    @Override
    public Object getRaw(ClanDataKey key) {
        return data.getOrDefault(key, key.defaultValue());
    }

    @Override
    public void set(ClanDataKey key, Object value) {
        data.put(key, value);
    }

    // ── Convenience getters ───────────────────────────────────────────────────

    public String getTag() { return getString(ClanDataKey.TAG); }

    public boolean isPvp() { return getBoolean(ClanDataKey.PVP); }

    public double getBalance() { return getDouble(ClanDataKey.BALANCE); }

    public int getMobKills() { return getInt(ClanDataKey.MOB_KILLS); }

    public int getPlayerKills() { return getInt(ClanDataKey.PLAYER_KILLS); }

    public int getOnlineTime() { return getInt(ClanDataKey.ONLINE_TIME); }

    public String getHomeString() { return getString(ClanDataKey.HOME); }

    public Location getHome() {
        String loc = getString(ClanDataKey.HOME);
        if ("none".equals(loc)) return null;
        String[] p = loc.split(";");
        return new Location(Bukkit.getWorld(p[0]),
                Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                Float.parseFloat(p[4]), Float.parseFloat(p[5]));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasHome() {
        return !"none".equals(getString(ClanDataKey.HOME));
    }

    // ── Mutators (update in-memory + persist) ─────────────────────────────────

    /**
     * Changes the clan leader and persists the change asynchronously.
     *
     * @param leader new leader's player name (original case)
     */
    public void setLeader(String leader) {
        this.leader = leader;
        core.getClanService().updateLeader(this);
    }

    /**
     * Serialises the location and persists it as the clan home asynchronously.
     *
     * @param location Bukkit {@link Location} to store
     */
    public void setHome(Location location) {
        String s = Objects.requireNonNull(location.getWorld()).getName()
                + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ()
                + ";" + location.getYaw() + ";" + location.getPitch();
        set(ClanDataKey.HOME, s);
        core.getClanDataService().updateHome(this);
    }

    /** Clears the clan home (sets to {@code "none"}) without persisting — call {@link #setHome} to persist. */
    public void removeHome() {
        set(ClanDataKey.HOME, "none");
    }

    public void setBalance(double balance) {
        set(ClanDataKey.BALANCE, balance);
        core.getClanDataService().updateBalance(this);
    }

    public void setPvp(boolean pvp) {
        set(ClanDataKey.PVP, pvp);
        core.getClanDataService().updatePvp(this);
    }

    public void addMobKill() {
        set(ClanDataKey.MOB_KILLS, getMobKills() + 1);
        core.getClanDataService().updateMobKills(this);
    }

    public void addPlayerKill() {
        set(ClanDataKey.PLAYER_KILLS, getPlayerKills() + 1);
        core.getClanDataService().updatePlayerKills(this);
    }

    public void addOnlineTime() {
        set(ClanDataKey.ONLINE_TIME, getOnlineTime() + 1);
        core.getClanDataService().updateOnlineTime(this);
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
     */
    public boolean hasLeader(String player) {
        return this.leader.equalsIgnoreCase(player);
    }

    /**
     * Returns {@code true} if {@code name} is a member of this clan.
     * Use in command handlers where only a player name is available.
     *
     * @param name player name to check
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
