package com.ie23s.bukkit.plugin.powerclans.configuration;

import com.ie23s.bukkit.plugin.powerclans.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Configuration {
    private static YamlConfiguration configuration;

    public static void loadConfig() {
        File configFile = new File(Main.plugin.getDataFolder(), "config.yml");
        YAMLHandler.exportConfigFile(configFile);
        YAMLHandler.checkConfigFile(configFile);
        configuration = YamlConfiguration.loadConfiguration(configFile);
    }

    public static YamlConfiguration getConfiguration() {
        return configuration;
    }
}
