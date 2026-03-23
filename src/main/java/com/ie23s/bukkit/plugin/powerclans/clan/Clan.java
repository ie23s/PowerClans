package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.api.IClan;
import com.ie23s.bukkit.plugin.powerclans.api.IClanData;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Clan implements IClan, IClanData {

    private final Core core;

    // clan_list fields
    private final String uuid;
    private final String name;
    private String leader;
    private int maxPlayers;
    private int level;

    // clan_data fields
    private final Map<ClanDataKey, Object> data = new EnumMap<>(ClanDataKey.class);

    public Clan(Core core, String uuid, String name, String leader, int maxPlayers, int level) {
        this.core = core;
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

    public void setLeader(String leader) {
        this.leader = leader.toLowerCase();
        core.getClanService().updateLeader(this);
    }

    public void setHome(Location location) {
        String s = Objects.requireNonNull(location.getWorld()).getName()
                + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ()
                + ";" + location.getYaw() + ";" + location.getPitch();
        set(ClanDataKey.HOME, s);
        core.getClanDataService().updateHome(this);
    }

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

    public void upgrade(int i) {
        this.maxPlayers += i;
        core.getClanService().updateMaxPlayers(this);
    }

    public void addLevel() {
        ++this.level;
        core.getClanService().updateLevel(this);
    }

    // ── Member management ─────────────────────────────────────────────────────

    public void invite(String name) {
        Member member = new Member(name, false, this.name);
        core.getMemberList().addMember(member);
        core.getMemberService().create(member);
    }

    public void kick(String name) {
        core.getMemberService().delete(core.getMemberList().getMember(name));
        core.getMemberList().removeMember(name);
    }

    public void setModer(String name, boolean isModer) {
        Member member = core.getMemberList().getMember(name);
        member.setModer(isModer);
        core.getMemberService().updateModer(member);
    }

    public boolean hasModer(String name) {
        Member member = core.getMemberList().getMember(name);
        return member.isModer() && member.getClan().equals(this.name);
    }

    public void disband() {
        for (String mem : core.getMemberList().getListOfMembers(this.name))
            kick(mem);
        core.getClanList().getClans().remove(this.name.toLowerCase());
        core.getMemberService().deleteByClan(this.name);
        core.getClanService().delete(this);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean hasLeader(String player) {
        return this.leader.equalsIgnoreCase(player);
    }

    public boolean hasClanMember(String name) {
        Member member = core.getMemberList().getMember(name);
        return member.getClan().equals(this.name);
    }

    public void broadcast(String message) {
        for (String member : core.getMemberList().getListOfMembers(this.name)) {
            if (Bukkit.getOfflinePlayer(member).isOnline()) {
                Objects.requireNonNull(Bukkit.getPlayer(member))
                       .sendMessage(core.lang("command.broadcast_format", core.lang("chat.clan"), message));
            }
        }
    }
}
