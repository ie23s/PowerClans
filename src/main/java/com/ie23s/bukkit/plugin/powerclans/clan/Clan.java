package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class Clan {
    private final Core core;

    private final String name;
    private final String tag;
    private String leader;
    private final Home home;
    private int maxplayers;
    private boolean pvp;
    private double balance;

    private int onlineTime;
    private int mobkills;
    private int playerkills;
    private int level;

    public Clan(Core core, String name, String tag, String leader, String home, int maxplayers, boolean pvp,
                double balance, int mobkills, int playerkills, int onlineTime, int level) {
        this.core = core;

        this.name = name;
        this.leader = leader;
        this.tag = tag;
        this.home = new Home(home);
        this.maxplayers = maxplayers;
        this.pvp = pvp;
        this.balance = balance;
        this.mobkills = mobkills;
        this.playerkills = playerkills;
        this.onlineTime = onlineTime;
        this.level = level;
    }

    public String getName() {
        return this.name;
    }

    public String getLeader() {
        return this.leader;
    }

    public void setLeader(String leader) {
        this.leader = leader.toLowerCase();
        core.getDb().setLeader(core.getMemberList().getMember(leader));
    }

    public String getTag() {
        return this.tag;
    }

    public boolean isPvp() {
        return pvp;
    }

    public Location getHome() {
        return this.home.getHome();
    }

    public void setHome(Location location) {
        this.home.setHome(location);
        core.getDb().setHome(this);
    }

    public void removeHome() {
        this.home.removeHome();
    }

    public int getMaxPlayers() {
        return this.maxplayers;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        core.getDb().setBalance(this);
    }

    public boolean isPvP() {
        return this.pvp;
    }

    public void setPvP(boolean pvp) {
        this.pvp = pvp;
        core.getDb().setPvP(this);
    }

    public void invite(String name) {
        Member member = new Member(name, false, this.name);
        core.getMemberList().addMember(member);
        core.getDb().createClanMember(member);
    }

    public void kick(String name) {
        core.getDb().kick(core.getMemberList().getMember(name));
        core.getMemberList().removeMember(name);

    }

    public void setModer(String name, boolean isModer) {

        Member member = core.getMemberList().getMember(name);
        member.setModer(isModer);
        core.getDb().setModer(member);

    }

    public boolean hasModer(String name) {
        Member member = core.getMemberList().getMember(name);
        return member.isModer() && member.getClan().equals(this.name);
    }

    public void disband() {
        for (String mem : core.getMemberList().getListOfMembers(this.name))
            kick(mem);
        core.getClanList().getClans().remove(this.name);
        core.getDb().disband(this.name);
    }

    public boolean hasLeader(String player) {
        return this.getLeader().equalsIgnoreCase(player);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasHome() {
        return this.home.hasHome();
    }

    public boolean hasClanMember(String name) {

        Member member = core.getMemberList().getMember(name);

        return member.getClan().equals(this.name);

    }

    public void upgrade(int i) {
        this.maxplayers += i;
        core.getDb().clanUpgrade(this);
    }

    public void broadcast(String message) {
        for (String member : core.getMemberList().getListOfMembers(this.name)) {
            if (Bukkit.getOfflinePlayer(member).isOnline()) {
                Objects.requireNonNull(Bukkit.getPlayer(member)).sendMessage(core.getLang().getMessage("command.broadcast_format", core.getLang().getMessage("chat.clan"), message));
            }
        }

    }

    public int getMobKills() {
        return mobkills;
    }

    public void addMobKill() {
        ++this.mobkills;
        core.getDb().setMobKills(this);
    }

    public int getPlayerKills() {
        return playerkills;
    }

    public void addPlayerKill() {
        ++this.playerkills;
        core.getDb().setPlayerKills(this);
    }

    public int getOnlineTime() {
        return onlineTime;
    }

    public void addOnlineTime() {
        ++this.onlineTime;
        core.getDb().setOnlineTime(this);
    }

    public void addLevel() {
        ++this.level;
        core.getDb().setLevel(this);
    }

    public int getLevel() {
        return level;
    }

    public String getHomeString() {
        return this.home.getStringLocation();
    }

    static class Home {
        private boolean hasHome = true;
        private Location location;
        private String stringLocation;

        Home(String location) {

            if (location.equals("none")) {
                hasHome = false;
                stringLocation = location;
                return;
            }

            String[] cords = location.split(";");
            this.location = new Location(Bukkit.getWorld(cords[0]),
                    Double.parseDouble(cords[1]), Double.parseDouble(cords[2]), Double.parseDouble(cords[3]),
                    Float.parseFloat(cords[4]), Float.parseFloat(cords[5]));
            this.stringLocation = location;
        }

        Location getHome() {
            return this.location;
        }

        void setHome(Location loc) {
            this.hasHome = true;
            this.location = loc;
            this.stringLocation = Objects.requireNonNull(loc.getWorld()).getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
        }

        void removeHome() {
            stringLocation = "none";
            hasHome = false;
        }

        boolean hasHome() {
            return hasHome;
        }

        String getStringLocation() {
            return this.stringLocation;
        }


    }
}