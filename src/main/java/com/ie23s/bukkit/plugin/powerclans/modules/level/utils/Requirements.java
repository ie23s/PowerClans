package com.ie23s.bukkit.plugin.powerclans.modules.level.utils;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.modules.level.Level;
import com.ie23s.bukkit.plugin.powerclans.modules.level.models.LevelMod;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Requirements {
    private final Level level;

    public Requirements(Level level) {
        this.level = level;
    }

    public boolean canUpgrade(Clan clan) {
        int clanLevel = clan.getLevel();

        LevelMod levelMod = level.getLevels().get(clanLevel);

        if (levelMod.getClanBalance() > 0) {
            if (levelMod.getClanBalance() > clan.getBalance())
                return false;
        }
        if (levelMod.getMobKills() > 0) {
            if (levelMod.getMobKills() > clan.getMobKills())
                return false;
        }
        if (levelMod.getPlayerKills() > 0) {
            if (levelMod.getPlayerKills() > clan.getPlayerKills())
                return false;
        }
        if (levelMod.getPlayedTime() > 0) {
            if (levelMod.getPlayedTime() > clan.getOnlineTime())
                return false;
        }
        if (levelMod.getClanMembers() > 0) {
            return levelMod.getClanMembers() <= level.getCore().getMemberList().getListOfMembers(clan.getName()).size();
        }
        return true;
    }

    public void upgradeRequirements(Player player) {
        Clan clan = level.getCore().getClanList().getClanByName(player.getName());

        int clanLevel = clan.getLevel();

        LevelMod levelMod = level.getLevels().get(clanLevel + 1);

        Language lang = level.getCore().getLang();

        ArrayList<String> messages = new ArrayList<>();
        messages.add(lang.getMessage("level.upgrade.need"));
        if (levelMod.getClanBalance() > 0) {
            int neededBalance = levelMod.getClanBalance();
            int clanBalance = (int) clan.getBalance();
            ChatColor chatColor = ChatColor.GREEN;
            if (neededBalance > clanBalance)
                chatColor = ChatColor.RED;
            messages.add(lang.getMessage("level.requirements.clanBalance", String.valueOf(clanBalance), String.valueOf(neededBalance)));
        }

        if (levelMod.getMobKills() > 0) {
            int neededKills = levelMod.getMobKills();
            int clanKills = clan.getMobKills();
            ChatColor chatColor = ChatColor.GREEN;
            if (neededKills > clanKills)
                chatColor = ChatColor.RED;
            messages.add(lang.getMessage("level.requirements.mobKills", chatColor.toString(), clanKills, neededKills));
        }

        if (levelMod.getPlayerKills() > 0) {
            int neededKills = levelMod.getPlayerKills();
            int clanKills = clan.getPlayerKills();
            ChatColor chatColor = ChatColor.GREEN;
            if (neededKills > clanKills)
                chatColor = ChatColor.RED;
            messages.add(lang.getMessage("level.requirements.playerKills", chatColor.toString(), clanKills, neededKills));
        }

        if (levelMod.getPlayedTime() > 0) {
            int neededTime = levelMod.getPlayedTime();
            int clanTime = clan.getOnlineTime();
            ChatColor chatColor = ChatColor.GREEN;
            if (neededTime > clanTime)
                chatColor = ChatColor.RED;
            messages.add(lang.getMessage("level.requirements.playedTime", chatColor.toString(), clanTime, neededTime));
        }

        if (levelMod.getClanMembers() > 0) {
            int neededMembers = levelMod.getClanMembers();
            int clanMembers = level.getCore().getMemberList().getListOfMembers(clan.getName()).size();
            ChatColor chatColor = ChatColor.GREEN;
            if (neededMembers > clanMembers)
                chatColor = ChatColor.RED;
            messages.add(lang.getMessage("level.requirements.clanBalance", chatColor.toString(), clanMembers, neededMembers));
        }
        String[] send = new String[messages.size()];
        messages.toArray(send);
        player.sendMessage(send);
    }

    public int upgradeCost(Clan clan) {
        int clanLevel = clan.getLevel();

        return level.getLevels().get(clanLevel + 1).getCost();
    }
}
