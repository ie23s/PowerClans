package com.ie23s.bukkit.plugin.powerclans.modules.level.models;

public class LevelMod {
    private final int cost;

    private final int mobKills;
    private final int playerKills;
    private final int playedTime;
    private final int clanBalance;
    private final int clanMembers;

    private final int addMembers;

    public LevelMod(int cost, int mobKills, int playerKills, int playedTime, int clanBalance, int clanMembers, int addMembers) {
        this.cost = cost;
        this.mobKills = mobKills;
        this.playerKills = playerKills;
        this.playedTime = playedTime;
        this.clanBalance = clanBalance;
        this.clanMembers = clanMembers;
        this.addMembers = addMembers;
    }

    public int getCost() {
        return cost;
    }

    public int getMobKills() {
        return mobKills;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public int getPlayedTime() {
        return playedTime;
    }

    public int getClanBalance() {
        return clanBalance;
    }

    public int getClanMembers() {
        return clanMembers;
    }

    public int getAddMembers() {
        return addMembers;
    }
}
