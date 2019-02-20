package com.ie23s.bukkit.plugin.powerclans.configuration;

import com.ie23s.bukkit.plugin.powerclans.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class Language {

    private static Map<String, String> language = new HashMap<>();

    public static void loadLang() {

        File configFile = new File(Main.plugin.getDataFolder(), "language_" + Configuration.getConfiguration().getString("language") + ".yml");
        YAMLHandler.exportConfigFile(configFile, "language_en.yml");
        YAMLHandler.checkConfigFile(configFile, "language_en.yml");
        YamlConfiguration langYml = YamlConfiguration.loadConfiguration(configFile);


        for (Entry<String, Object> stringObjectEntry : langYml.getValues(false).entrySet()) {
            if (stringObjectEntry.getValue() instanceof ConfigurationSection) {
                for (Entry<String, Object> stringObjectEntry1 : ((ConfigurationSection) stringObjectEntry.getValue()).getValues(false).entrySet()) {
                    language.put(((Entry) stringObjectEntry).getKey() + "." + ((Entry) stringObjectEntry1).getKey(), String.valueOf(stringObjectEntry1.getValue()));
                }
            } else {
                language.put(String.valueOf(((Entry) stringObjectEntry).getKey()), String.valueOf(stringObjectEntry.getValue()));
            }
        }

    }

    public static String getMessage(String target) {
        return language.get(target);
    }

    public static String getMessage(String target, Object... arg1) {
        return String.format(language.get(target), arg1);
    }
}
