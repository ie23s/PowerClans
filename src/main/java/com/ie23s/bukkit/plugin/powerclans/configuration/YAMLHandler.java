package com.ie23s.bukkit.plugin.powerclans.configuration;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.utils.Logger;
import org.apache.commons.lang.SystemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

class YAMLHandler {

    static void exportConfigFile(File file) {
        InputStream inputStream;
        FileOutputStream outputStream;
        byte[] buff;
        int n;

        if (!file.exists()) {
            inputStream = Main.class.getResourceAsStream("/" + file.getName());

            try {
                outputStream = new FileOutputStream(file);
                buff = new byte[65536];

                while ((n = inputStream.read(buff)) > 0) {
                    outputStream.write(buff, 0, n);
                    outputStream.flush();
                }
                outputStream.close();


                if (SystemUtils.IS_OS_WINDOWS) {
                    winConvert(file);
                }

            } catch (Exception var7) {
                Logger.error(var7);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    static void exportConfigFile(File file, String resource) {
        InputStream inputStream;
        FileOutputStream outputStream;
        byte[] buff;
        int n;

        if (!file.exists()) {
            inputStream = Main.class.getResourceAsStream("/" + resource);

            try {
                outputStream = new FileOutputStream(file);
                buff = new byte[65536];

                while ((n = inputStream.read(buff)) > 0) {
                    outputStream.write(buff, 0, n);
                    outputStream.flush();
                }
                outputStream.close();


                if (SystemUtils.IS_OS_WINDOWS) {
                    winConvert(file);
                }

            } catch (Exception var7) {
                Logger.error(var7);
            }
        }
    }

    @SuppressWarnings("deprecation")
    static void checkConfigFile(File configFile) {
        YamlConfiguration defaultYamlConfiguration = YamlConfiguration.loadConfiguration(Main.class.getResourceAsStream("/" + configFile.getName()));
        YamlConfiguration file = YamlConfiguration.loadConfiguration(configFile);
        ArrayList<String> defaultConfig = getArrayConfig(defaultYamlConfiguration);
        ArrayList<String> realConfig = getArrayConfig(file);

        boolean save = false;

        for (String option : defaultConfig) {
            if (!realConfig.contains(option)) {
                file.set(option, defaultYamlConfiguration.get(option));
                save = true;
            }
        }
        if (save) {
            try {
                file.save(configFile);
            } catch (Exception ignore) {
            }
        }
    }

    @SuppressWarnings({"deprecation", "SameParameterValue"})
    static void checkConfigFile(File configFile, String resource) {
        YamlConfiguration defaultYamlConfiguration = YamlConfiguration.loadConfiguration(Main.class.getResourceAsStream("/" + resource));
        YamlConfiguration file = YamlConfiguration.loadConfiguration(configFile);
        ArrayList<String> defaultConfig = getArrayConfig(defaultYamlConfiguration);
        ArrayList<String> realConfig = getArrayConfig(file);

        boolean save = false;

        for (String option : defaultConfig) {
            if (!realConfig.contains(option)) {
                file.set(option, defaultYamlConfiguration.get(option));
                save = true;
            }
        }
        if (save) {
            try {
                file.save(configFile);
            } catch (Exception ignore) {
            }
        }
    }

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
        CharBuffer cb = Charset.forName("UTF-8").decode(bb);
        bb = Charset.forName("windows-1251").encode(cb);
        Files.write(path, bb.array());
    }
//    //TODO Convert
//    private static void winUnconvert(File file) throws Exception {
//        Path path = Paths.get(file.toURI());
//        ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(path));
//        CharBuffer cb = Charset.forName("windows-1251").decode(bb);
//        bb = Charset.forName("UTF-8").encode(cb);
//        Files.write(path, bb.array());
//    }
}
