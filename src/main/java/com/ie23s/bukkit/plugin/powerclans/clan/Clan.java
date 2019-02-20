package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.database.Init;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Objects;

public class Clan {

    private static HashMap<String, Clan> clans = new HashMap<>();
    private String name;
    private String tag;
    private String leader;
    private Home home;
    private int maxplayers;
    private boolean pvp;
    private double balance;


    @SuppressWarnings("rawtypes")
    public Clan(String name, String tag, String leader, String home, int maxplayers, boolean pvp, double balance) {
        this.name = name;
        this.leader = leader;
        this.tag = tag;
        this.home = new Home(home);
        this.maxplayers = maxplayers;
        this.pvp = pvp;
        this.balance = balance;
    }

    public static Clan getClan(String clan) {
        return clans.get(clan.toLowerCase());
    }

    public static Clan getClanByName(String player) {
        if (Member.isMember(player)) {
            return getClan(Objects.requireNonNull(Member.getMember(player)).getClan());
        }

        return null;
    }

    public static Clan create(String clan, String leader) {
        Member member = new Member(leader.toLowerCase(), false, clan);
        Clan c = new Clan(ChatColor.stripColor(clan.replaceAll("&", "§")), clan, leader.toLowerCase(), "none", Configuration.getConfiguration().getInt("settings.default_max"), true, 0);
        clans.put(clan.toLowerCase(), c);
        Member.addMember(member);
        Init.getConnection().createClan(c);
        Init.getConnection().createClanMember(member);
        return c;
    }

    public static int number() {
        return clans.size();
    }

    public static HashMap<String, Clan> getClans() {
        return clans;
    }

    public String getName() {
        return this.name;
    }

    public String getLeader() {
        return this.leader;
    }

    public void setLeader(String leader) {
        this.leader = leader.toLowerCase();
        Init.getConnection().setLeader(Member.getMember(leader));
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
        Init.getConnection().setHome(this);
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
        Init.getConnection().setBalance(this);
    }

    public boolean isPvP() {
        return this.pvp;
    }

    public void setPvP(boolean pvp) {
        this.pvp = pvp;
        Init.getConnection().setPvP(this);
    }

    public void invite(String name) {
        Member member = new Member(name, false, this.name);
        Member.addMember(member);
        Init.getConnection().createClanMember(member);
    }

    public void kick(String name) {
        Init.getConnection().kick(Member.getMember(name));
        Member.removeMember(name);

    }

    public void setModer(String name, boolean isModer) {

        Member member = Member.getMember(name);
        member.setModer(isModer);
        Init.getConnection().setModer(member);

    }

    @SuppressWarnings("rawtypes")
    public boolean hasModer(String name) {
        Member member = Member.getMember(name);
        return member.isModer() && member.getClan().equals(this.name);
    }

    public void disband() {
        for (String mem : Member.getListOfMembers(this.name))
            kick(mem);
        clans.remove(this.name);
        Init.getConnection().disband(this.name);
    }

    public boolean hasLeader(String player) {
        return this.getLeader().equalsIgnoreCase(player);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasHome() {
        return this.home.hasHome();
    }

    public boolean hasClanMember(String name) {

        Member member = Member.getMember(name);

        return member.getClan().equals(this.name);

    }

    public void upgrade(int i) {
        this.maxplayers += i;
        Init.getConnection().clanUpgrade(this);
    }

    public void setMaxplayers(int i) {
        this.maxplayers = i;
        Init.getConnection().clanUpgrade(this);
    }

    @SuppressWarnings("deprecation")
    public void broadcast(String message) {
        for (String member : Member.getListOfMembers(this.name)) {
            if (Bukkit.getOfflinePlayer(member).isOnline()) {
                Bukkit.getPlayer(member).sendMessage(Language.getMessage("command.broadcast_format", Language.getMessage("chat.clan"), message));
            }
        }

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
            this.stringLocation = loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
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