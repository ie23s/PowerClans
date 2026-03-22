package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class LifecycleCommandsTest {

    @Mock
    Core core;

    @Mock
    Clan clan;

    @Mock
    Player sender;

    @Mock
    ClanList clanList;

    @Mock
    FileConfiguration config;

    @BeforeEach
    void setUp() {
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), (Object[]) any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(sender.hasPermission(anyString())).thenReturn(true);
        lenient().when(sender.getName()).thenReturn("leader");
        lenient().when(core.getClanList()).thenReturn(clanList);
        lenient().when(core.getConfig()).thenReturn(config);
    }

    // -------------------------------------------------------------------------
    // Create.validate
    // -------------------------------------------------------------------------

    @Test
    void create_validate_failsWhenNoPermission() {
        when(sender.hasPermission("PowerClans.create")).thenReturn(false);
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "MyClan"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._1");
    }

    @Test
    void create_validate_failsWhenNoNameArgument() {
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._2");
    }

    @Test
    void create_validate_failsWhenAlreadyInClan() {
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "MyClan"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._3");
    }

    @Test
    void create_validate_failsWhenClanNameAlreadyTaken() {
        when(clanList.getClan("MyClan")).thenReturn(clan);
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "MyClan"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._4");
    }

    @Test
    void create_validate_failsWhenNameTooLong() {
        when(clanList.getClan(anyString())).thenReturn(null);
        when(config.getInt("settings.max_symbols")).thenReturn(3);
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "TOOLONGNAME"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._5");
    }

    @Test
    void create_validate_failsWhenNameTooShort() {
        when(clanList.getClan(anyString())).thenReturn(null);
        when(config.getInt("settings.max_symbols")).thenReturn(20);
        when(config.getInt("settings.min_symbols")).thenReturn(5);
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "ab"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._6");
    }

    @Test
    void create_validate_failsWhenNameDoesNotMatchRegex() {
        when(clanList.getClan(anyString())).thenReturn(null);
        when(config.getInt("settings.max_symbols")).thenReturn(20);
        when(config.getInt("settings.min_symbols")).thenReturn(1);
        when(config.getString("settings.clan_regex")).thenReturn("[a-z]+");
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "123"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._7");
    }

    @Test
    void create_validate_passesForValidNewClan() {
        when(clanList.getClan(anyString())).thenReturn(null);
        when(config.getInt("settings.max_symbols")).thenReturn(20);
        when(config.getInt("settings.min_symbols")).thenReturn(1);
        when(config.getString("settings.clan_regex")).thenReturn("[a-zA-Z0-9]+");
        when(config.getInt("settings.create_cost")).thenReturn(0);
        LifecycleCommands.Create cmd = new LifecycleCommands.Create(core);

        boolean result = cmd.validate(sender, new String[]{"create", "ValidName"}, null, "leader");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    // Disband.validate
    // -------------------------------------------------------------------------

    @Test
    void disband_validate_failsWhenNotInClan() {
        LifecycleCommands.Disband cmd = new LifecycleCommands.Disband(core);

        boolean result = cmd.validate(sender, new String[]{"disband"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void disband_validate_failsWhenNotLeader() {
        when(sender.getName()).thenReturn("member");
        when(clan.hasLeader("member")).thenReturn(false);
        LifecycleCommands.Disband cmd = new LifecycleCommands.Disband(core);

        boolean result = cmd.validate(sender, new String[]{"disband"}, clan, "member");

        assertFalse(result);
        verify(sender).sendMessage("errors._10");
    }

    @Test
    void disband_validate_passesForLeader() {
        when(sender.getName()).thenReturn("leader");
        when(clan.hasLeader("leader")).thenReturn(true);
        LifecycleCommands.Disband cmd = new LifecycleCommands.Disband(core);

        boolean result = cmd.validate(sender, new String[]{"disband"}, clan, "leader");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    // Leave.validate
    // -------------------------------------------------------------------------

    @Test
    void leave_validate_failsWhenNotInClan() {
        LifecycleCommands.Leave cmd = new LifecycleCommands.Leave(core);

        boolean result = cmd.validate(sender, new String[]{"leave"}, null, "member");

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void leave_validate_failsWhenSenderIsLeader() {
        when(sender.getName()).thenReturn("leader");
        when(clan.hasLeader("leader")).thenReturn(true);
        LifecycleCommands.Leave cmd = new LifecycleCommands.Leave(core);

        boolean result = cmd.validate(sender, new String[]{"leave"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._27");
    }

    @Test
    void leave_validate_passesForNonLeaderMember() {
        when(sender.getName()).thenReturn("member");
        when(clan.hasLeader("member")).thenReturn(false);
        LifecycleCommands.Leave cmd = new LifecycleCommands.Leave(core);

        boolean result = cmd.validate(sender, new String[]{"leave"}, clan, "member");

        assertTrue(result);
    }
}
