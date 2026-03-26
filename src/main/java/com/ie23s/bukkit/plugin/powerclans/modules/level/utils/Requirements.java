package com.ie23s.bukkit.plugin.powerclans.modules.level.utils;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.modules.level.Level;
import com.ie23s.bukkit.plugin.powerclans.modules.level.models.LevelMod;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/** Checks and displays clan level-up requirements. */
public class Requirements {
    private final Level level;

    /** @param level the level module this helper belongs to */
    public Requirements(Level level) {
        this.level = level;
    }

    /**
     * Returns {@code true} if the clan meets all requirements for the next level.
     *
     * @param clan the clan to check
     */
    public boolean canUpgrade(Clan clan) {
        int clanLevel = clan.getLevel();

        LevelMod levelMod = level.getLevels().get(clanLevel + 1);

        if (clanLevel == level.getMaxLevel())
            return false;

        if (levelMod.getClanBalance() > 0 && levelMod.getClanBalance() > clan.getDouble(ClanDataKey.BALANCE))
            return false;
        if (levelMod.getMobKills() > 0 && levelMod.getMobKills() > clan.getInt(ClanDataKey.MOB_KILLS))
            return false;
        if (levelMod.getPlayerKills() > 0 && levelMod.getPlayerKills() > clan.getInt(ClanDataKey.PLAYER_KILLS))
            return false;
        if (levelMod.getPlayedTime() > 0 && levelMod.getPlayedTime() > clan.getInt(ClanDataKey.ONLINE_TIME))
            return false;
        if (levelMod.getClanMembers() > 0) {
            return levelMod.getClanMembers() <= level.getCore().getMemberList().getListOfMembers(clan.getName()).size();
        }
        return true;
    }

    /**
     * Sends the player a coloured summary of all next-level requirements
     * showing current vs. required values.
     *
     * @param player the player to send the summary to
     */
    public void upgradeRequirements(Player player) {
        Language lang = level.getCore().getLang();
        Clan clan = level.getCore().getClanList().getClanByName(player.getName());

        int clanLevel = clan.getLevel();
        if (clanLevel == level.getMaxLevel()) {
            player.sendMessage(lang.getMessage("level.upgrade.max"));
            return;
        }
        LevelMod levelMod = level.getLevels().get(clanLevel + 1);


        ArrayList<String> messages = new ArrayList<>();
        messages.add(lang.getMessage("level.upgrade.need"));
        if (levelMod.getClanBalance() > 0)
            messages.add(lang.getMessage("level.requirements.clan_balance",
                    progress((int) clan.getDouble(ClanDataKey.BALANCE), levelMod.getClanBalance())));
        if (levelMod.getMobKills() > 0)
            messages.add(lang.getMessage("level.requirements.mob_kills",
                    progress(clan.getInt(ClanDataKey.MOB_KILLS), levelMod.getMobKills())));
        if (levelMod.getPlayerKills() > 0)
            messages.add(lang.getMessage("level.requirements.player_kills",
                    progress(clan.getInt(ClanDataKey.PLAYER_KILLS), levelMod.getPlayerKills())));
        if (levelMod.getPlayedTime() > 0)
            messages.add(lang.getMessage("level.requirements.played_time",
                    progress(clan.getInt(ClanDataKey.ONLINE_TIME), levelMod.getPlayedTime())));
        if (levelMod.getClanMembers() > 0)
            messages.add(lang.getMessage("level.requirements.clan_members",
                    progress(level.getCore().getMemberList().getListOfMembers(clan.getName()).size(), levelMod.getClanMembers())));
        player.sendMessage(messages.toArray(new String[0]));
    }

    /**
     * Formats a coloured {@code actual/needed} progress string.
     *
     * @param actual current value
     * @param needed required value
     * @return green-coloured string if {@code actual >= needed}, red otherwise
     */
    private static String progress(int actual, int needed) {
        ChatColor color = actual >= needed ? ChatColor.GREEN : ChatColor.RED;
        return color + String.valueOf(actual) + "/" + needed;
    }

    /**
     * Returns the gold cost required to upgrade the clan to the next level.
     *
     * @param clan the clan being upgraded
     * @return the cost defined in the next level's {@link com.ie23s.bukkit.plugin.powerclans.modules.level.models.LevelMod}
     */
    public int upgradeCost(Clan clan) {
        int clanLevel = clan.getLevel();

        return level.getLevels().get(clanLevel + 1).getCost();
    }
}
