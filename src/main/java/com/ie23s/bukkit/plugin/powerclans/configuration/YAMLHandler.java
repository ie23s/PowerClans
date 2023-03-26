package com.ie23s.bukkit.plugin.powerclans.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class YAMLHandler {

    final Plugin plugin;

    public YAMLHandler(Plugin plugin) {
        this.plugin = plugin;
    }

//    static void exportConfigFile(File file) {
//        InputStream inputStream;
//        FileOutputStream outputStream;
//        byte[] buff;
//        int n;
//
//        if (!file.exists()) {
//            inputStream = Main.class.getResourceAsStream("/" + file.getName());
//
//            try {
//                outputStream = new FileOutputStream(file);
//                buff = new byte[65536];
//
//                while ((n = inputStream.read(buff)) > 0) {
//                    outputStream.write(buff, 0, n);
//                    outputStream.flush();
//                }
//                outputStream.close();
//
//
//                if (SystemUtils.IS_OS_WINDOWS) {
//                    winConvert(file);
//                }
//
//            } catch (Exception var7) {
//                Logger.error(var7);
//            }
//        }
//    }
//
//    @SuppressWarnings("SameParameterValue")
//    static void exportConfigFile(File file, String resource) {
//        InputStream inputStream;
//        FileOutputStream outputStream;
//        byte[] buff;
//        int n;
//
//        if (!file.exists()) {
//            inputStream = Main.class.getResourceAsStream("/" + resource);
//
//            try {
//                outputStream = new FileOutputStream(file);
//                buff = new byte[65536];
//
//                while ((n = inputStream.read(buff)) > 0) {
//                    outputStream.write(buff, 0, n);
//                    outputStream.flush();
//                }
//                outputStream.close();
//
//
//                if (SystemUtils.IS_OS_WINDOWS) {
//                    winConvert(file);
//                }
//
//            } catch (Exception var7) {
//                Logger.error(var7);
//            }
//        }
//    }
//
//    static void checkConfigFile(File configFile) {
//        YamlConfiguration defaultYamlConfiguration = YamlConfiguration.loadConfiguration(Main.class.getResourceAsStream("/" + configFile.getName()));
//        YamlConfiguration file = YamlConfiguration.loadConfiguration(configFile);
//        ArrayList<String> defaultConfig = getArrayConfig(defaultYamlConfiguration);
//        ArrayList<String> realConfig = getArrayConfig(file);
//
//        boolean save = false;
//
//        for (String option : defaultConfig) {
//            if (!realConfig.contains(option)) {
//                file.set(option, defaultYamlConfiguration.get(option));
//                save = true;
//            }
//        }
//        if (save) {
//            try {
//                file.save(configFile);
//            } catch (Exception ignore) {
//            }
//        }
//    }
//
//    @SuppressWarnings({"deprecation", "SameParameterValue"})
//    static void checkConfigFile(File configFile, String resource) {
//        YamlConfiguration defaultYamlConfiguration = YamlConfiguration.loadConfiguration(Main.class.getResourceAsStream("/" + resource));
//        YamlConfiguration file = YamlConfiguration.loadConfiguration(configFile);
//        ArrayList<String> defaultConfig = getArrayConfig(defaultYamlConfiguration);
//        ArrayList<String> realConfig = getArrayConfig(file);
//
//        boolean save = false;
//
//        for (String option : defaultConfig) {
//            if (!realConfig.contains(option)) {
//                file.set(option, defaultYamlConfiguration.get(option));
//                save = true;
//            }
//        }
//        if (save) {
//            try {
//                file.save(configFile);
//            } catch (Exception ignore) {
//            }
//        }
//    }

    private static ArrayList<String> getArrayConfig(YamlConfiguration yamlConfiguration) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Map.Entry<String, Object> stringObjectEntry : yamlConfiguration.getValues(false).entrySet()) {
            if (stringObjectEntry.getValue() instanceof ConfigurationSection) {
                arrayList.addAll(getArrayConfig(yamlConfiguration, stringObjectEntry.getKey()));
            } else {
                arrayList.add(stringObjectEntry.getKey());
            }
        }
        return arrayList;
    }

    private static ArrayList<String> getArrayConfig(YamlConfiguration yamlConfiguration, String path) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Map.Entry<String, Object> stringObjectEntry : yamlConfiguration.getConfigurationSection("" + path).getValues(false).entrySet()) {
            if (stringObjectEntry.getValue() instanceof ConfigurationSection) {
                arrayList.addAll(getArrayConfig(yamlConfiguration, path + "." + stringObjectEntry.getKey()));
            } else {
                arrayList.add(path + "." + stringObjectEntry.getKey());
            }
        }
        return arrayList;
    }

    private static void winConvert(File file) throws Exception {
        Path path = Paths.get(file.toURI());
        ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(path));
        CharBuffer cb = StandardCharsets.UTF_8.decode(bb);
        bb = Charset.forName("windows-1251").encode(cb);
        Files.write(path, bb.array());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public YamlConfiguration createCustomConfig(String configFileName) {
        File customConfigFile = new File(plugin.getDataFolder(), configFileName);
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            plugin.saveResource(configFileName, false);
        }

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return configuration;
    }
}
