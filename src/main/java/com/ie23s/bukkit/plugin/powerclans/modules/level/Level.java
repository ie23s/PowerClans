package com.ie23s.bukkit.plugin.powerclans.modules.level;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.modules.level.config.LevelsConfig;
import com.ie23s.bukkit.plugin.powerclans.modules.level.event.KillEvent;
import com.ie23s.bukkit.plugin.powerclans.modules.level.models.LevelMod;
import com.ie23s.bukkit.plugin.powerclans.modules.level.utils.Abilities;
import com.ie23s.bukkit.plugin.powerclans.modules.level.utils.PlayedTime;
import com.ie23s.bukkit.plugin.powerclans.modules.level.utils.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class Level {
    private final Core core;

    private final LevelsConfig levelsConfigClass;
    private final HashMap<Integer, LevelMod> levels = new HashMap<>();
    private FileConfiguration config;
    private Requirements requirements;
    private Abilities abilities;
    private int maxLevel;

    public Level(Core core) {
        this.core = core;
        levelsConfigClass = new LevelsConfig(core);
    }

    public void loadModule() {
        config = levelsConfigClass.loadConfig();
        loadLevels();
        requirements = new Requirements(this);
        abilities = new Abilities(this);

        Bukkit.getPluginManager().registerEvents(new KillEvent(core), core);
        Bukkit.getScheduler().runTaskTimerAsynchronously(core, new PlayedTime(core), 0, 1200);
    }

    public Core getCore() {
        return core;
    }

    public Requirements getRequirements() {
        return requirements;
    }

    public Abilities getAbilities() {
        return abilities;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public HashMap<Integer, LevelMod> getLevels() {
        return levels;
    }

    public void loadLevels() {
        for (int i = 2; ; i++) {
            if (!config.contains("levels." + i))
                break;

            LevelMod levelMod = new LevelMod(
                    config.getInt("levels." + i + ".cost"),
                    config.getInt("levels." + i + ".requirements.mobKills"),
                    config.getInt("levels." + i + ".requirements.playerKills"),
                    config.getInt("levels." + i + ".requirements.playedTime"),
                    config.getInt("levels." + i + ".requirements.clanBalance"),
                    config.getInt("levels." + i + ".requirements.clanMembers"),
                    config.getInt("levels." + i + ".abilities.addMembers")

            );
            levels.put(i, levelMod);
            maxLevel = i;
        }
    }
}
