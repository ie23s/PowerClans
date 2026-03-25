package com.ie23s.bukkit.plugin.powerclans.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class YAMLHandlerTest {

    @TempDir
    File dataFolder;

    @Mock
    Plugin plugin;

    YAMLHandler handler;

    @BeforeEach
    void setUp() {
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        handler = new YAMLHandler(plugin);
    }

    // ── File already exists ───────────────────────────────────────────────────

    @Test
    void loadsExistingFile_withoutCallingPluginSaveResource() throws IOException {
        File cfg = new File(dataFolder, "config.yml");
        Files.writeString(cfg.toPath(), "key: value\n");

        YamlConfiguration result = handler.createCustomConfig("config.yml");

        verify(plugin, never()).saveResource(any(), anyBoolean());
        assertEquals("value", result.getString("key"));
    }

    @Test
    void returnsCorrectValues_fromExistingFile() throws IOException {
        File cfg = new File(dataFolder, "config.yml");
        Files.writeString(cfg.toPath(), "settings:\n  pvp: true\n  max: 10\n");

        YamlConfiguration result = handler.createCustomConfig("config.yml");

        assertTrue(result.getBoolean("settings.pvp"));
        assertEquals(10, result.getInt("settings.max"));
    }

    // ── File does not exist ───────────────────────────────────────────────────

    @Test
    void callsSaveResource_whenFileAbsent() {
        doAnswer(inv -> {
            File cfg = new File(dataFolder, "config.yml");
            try { Files.writeString(cfg.toPath(), "key: created\n"); } catch (IOException ignored) {
                //No need to handle
            }
            return null;
        }).when(plugin).saveResource("config.yml", false);

        YamlConfiguration result = handler.createCustomConfig("config.yml");

        verify(plugin).saveResource("config.yml", false);
        assertEquals("created", result.getString("key"));
    }

    @Test
    void createsParentDirectories_whenAbsent() {
        doAnswer(inv -> {
            File cfg = new File(dataFolder, "sub/config.yml");
            try { Files.writeString(cfg.toPath(), "a: b\n"); } catch (IOException ignored) {
                //No need to handle
            }
            return null;
        }).when(plugin).saveResource("sub/config.yml", false);

        handler.createCustomConfig("sub/config.yml");

        assertTrue(new File(dataFolder, "sub").isDirectory());
    }

    // ── saveResource throws ───────────────────────────────────────────────────

    @Test
    void returnsEmptyConfig_whenResourceNotInJar() {
        doThrow(new IllegalArgumentException("not found"))
                .when(plugin).saveResource("missing.yml", false);

        YamlConfiguration result = handler.createCustomConfig("missing.yml");

        assertNotNull(result);
        assertTrue(result.getKeys(false).isEmpty());
    }

    // ── Invalid YAML ──────────────────────────────────────────────────────────

    @Test
    void returnsEmptyConfig_whenYamlIsInvalid() throws IOException {
        File cfg = new File(dataFolder, "bad.yml");
        Files.writeString(cfg.toPath(), "key: [\nbad yaml\n");

        YamlConfiguration result = handler.createCustomConfig("bad.yml");

        assertNotNull(result);
        assertTrue(result.getKeys(false).isEmpty());
    }
}
