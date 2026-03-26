package com.ie23s.bukkit.plugin.powerclans.modules.level.utils;

import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.modules.level.Level;
import com.ie23s.bukkit.plugin.powerclans.modules.level.models.LevelMod;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/** Builds and displays the list of abilities unlocked by the next clan level-up. */
public class Abilities {
    private final Level level;

    /** @param level the level module this helper belongs to */
    public Abilities(Level level) {
        this.level = level;
    }

    /**
     * Sends the player a preview of abilities for the current or next level.
     *
     * @param player the player to send the message to
     * @param next   {@code true} to show the next level's abilities, {@code false} for the current level
     */
    public void upgradeAbilities(Player player, boolean next) {
        Clan clan = level.getCore().getClanList().getClanByName(player.getName());

        int clanLevel = clan.getLevel();
        if (clanLevel == level.getMaxLevel()) {
            return;
        }
        int levelPlus = next ? 1 : 0;
        LevelMod levelMod = level.getLevels().get(clanLevel + levelPlus);

        Language lang = level.getCore().getLang();

        ArrayList<String> messages = new ArrayList<>();

        if (levelMod.getAddMembers() > 0) {
            int ability = levelMod.getAddMembers();
            ability += clan.getMaxPlayers();
            messages.add(lang.getMessage("level.abilities.add_members", ability));
        }
        String[] send = new String[messages.size()];
        messages.toArray(send);
        player.sendMessage(send);
    }

    /**
     * Applies the upgrade: deducts the cost, expands max members, and increments the clan level.
     *
     * @param clan the clan being upgraded
     */
    public void makeUpgrade(Clan clan) {
        int clanLevel = clan.getLevel();
        LevelMod levelMod = level.getLevels().get(clanLevel + 1);
        clan.update(ClanDataKey.BALANCE, clan.getDouble(ClanDataKey.BALANCE) - level.getRequirements().upgradeCost(clan));
        //Adding members
        clan.upgrade(levelMod.getClanMembers());
        clan.addLevel();
    }
}
