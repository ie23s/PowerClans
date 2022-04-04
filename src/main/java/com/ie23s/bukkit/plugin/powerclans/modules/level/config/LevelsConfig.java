package com.ie23s.bukkit.plugin.powerclans.modules.level.config;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.configuration.YAMLHandler;
import org.bukkit.configuration.file.FileConfiguration;

public class LevelsConfig {
    private final Core core;

    public LevelsConfig(Core core) {
        this.core = core;
    }

    public FileConfiguration loadConfig() {
        YAMLHandler yamlHandler = new YAMLHandler(core);
        return yamlHandler.createCustomConfig("levels.yml");

    }
}
