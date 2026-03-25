package com.ie23s.bukkit.plugin.powerclans.configuration;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Utility for loading YAML configuration files from the plugin's data folder.
 *
 * <p>If a requested file does not yet exist on disk, it is extracted from the
 * plugin's bundled resources before being loaded.
 */
public class YAMLHandler {

    private final Plugin plugin;

    /**
     * @param plugin the owning plugin; provides the data folder and bundled resource access
     */
    public YAMLHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads (or creates from plugin resources) a YAML configuration file.
     *
     * <p>If the file does not exist, the plugin's bundled resource with the same name is
     * copied to the data folder before loading. If the data-folder directory tree cannot
     * be created, a warning is logged and the method proceeds; any subsequent
     * {@link Plugin#saveResource} failure is also logged.
     *
     * @param configFileName path relative to {@link Plugin#getDataFolder()},
     *                       e.g. {@code "config.yml"}
     * @return the loaded {@link YamlConfiguration}, or an empty instance if parsing fails
     */
    public YamlConfiguration createCustomConfig(String configFileName) {
        File configFile = new File(plugin.getDataFolder(), configFileName);

        if (!configFile.exists()) {
            File parent = configFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                plugin.getLogger().warning(() -> "Could not create directory: " + parent);
            }
            try {
                plugin.saveResource(configFileName, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, e,
                        () -> "Resource not found in jar: " + configFileName);
            }
        }

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, e,
                    () -> "Failed to load config: " + configFileName);
        }
        return configuration;
    }
}
