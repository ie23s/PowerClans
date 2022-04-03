package com.ie23s.bukkit.plugin.powerclans.configuration;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class Language {
    private final Core core;
    private final Map<String, String> language = new HashMap<>();

    public Language(Core core) {
        this.core = core;
    }

    public void loadLang() {
        YAMLHandler yamlHandler = new YAMLHandler(core);
        FileConfiguration langYml = yamlHandler.createCustomConfig("language_en.yml");


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

    public String getMessage(String target) {
        return language.get(target);
    }

    public String getMessage(String target, Object... arg1) {
        return String.format(language.get(target), arg1);
    }
}
