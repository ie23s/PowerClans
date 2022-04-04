package com.ie23s.bukkit.plugin.powerclans.modules.level.utils;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.modules.level.Level;
import com.ie23s.bukkit.plugin.powerclans.modules.level.models.LevelMod;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Abilities {
    private final Level level;

    public Abilities(Level level) {
        this.level = level;
    }

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

    public void makeUpgrade(Clan clan) {
        int clanLevel = clan.getLevel();
        LevelMod levelMod = level.getLevels().get(clanLevel + 1);
        //Adding members
        clan.upgrade(levelMod.getClanMembers());
        clan.addLevel();
        clan.setBalance(clan.getBalance() - level.getRequirements().upgradeCost(clan));
    }
}
