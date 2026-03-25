package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class ChatListenerTest {

    @Mock Core core;
    @Mock FileConfiguration config;
    @Mock ClanList clanList;
    @Mock MemberList memberList;
    @Mock Clan clan;
    @Mock Player player;

    ChatListener listener;

    @BeforeEach
    void setUp() {
        when(core.getConfig()).thenReturn(config);
        when(core.getClanList()).thenReturn(clanList);
        when(core.getMemberList()).thenReturn(memberList);
        when(player.getName()).thenReturn("alice");
        when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(core.lang(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        listener = new ChatListener(core);
    }

    private AsyncPlayerChatEvent chatEvent(String format, String message) {
        AsyncPlayerChatEvent event = mock(AsyncPlayerChatEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getFormat()).thenReturn(format);
        when(event.getMessage()).thenReturn(message);
        when(event.getRecipients()).thenReturn(new HashSet<>());
        return event;
    }

    // ── substituteClanTag ─────────────────────────────────────────────────────

    @Test
    void substituteClanTag_replacesClanTagPlaceholder_forMember() {
        when(memberList.isMember("alice")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(clan);
        when(clan.getTag()).thenReturn("WAR");

        AsyncPlayerChatEvent event = chatEvent("<%player%> [!clantag!]", "hello");
        listener.substituteClanTag(event);

        verify(event).setFormat("<%player%> [WAR]");
    }

    @Test
    void substituteClanTag_removesPlaceholder_forNonMember() {
        when(memberList.isMember("alice")).thenReturn(false);

        AsyncPlayerChatEvent event = chatEvent("<%player%> [!clantag!]", "hello");
        listener.substituteClanTag(event);

        verify(event).setFormat("<%player%> []");
    }

    @Test
    void substituteClanTag_doesNothing_whenPlaceholderAbsent() {
        AsyncPlayerChatEvent event = chatEvent("<%player%> %2$s", "hello");
        listener.substituteClanTag(event);

        verify(event, never()).setFormat(any());
    }

    // ── handleClanChat ────────────────────────────────────────────────────────

    @Test
    void handleClanChat_doesNothing_whenClanChatDisabled() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(false);

        AsyncPlayerChatEvent event = chatEvent("format", "%hello clan");
        listener.handleClanChat(event);

        verify(event, never()).setCancelled(anyBoolean());
        verify(event, never()).setFormat(any());
    }

    @Test
    void handleClanChat_doesNothing_whenMessageLacksPrefix() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);

        AsyncPlayerChatEvent event = chatEvent("format", "normal message");
        listener.handleClanChat(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void handleClanChat_doesNothing_whenMessageIsJustPercent() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);

        AsyncPlayerChatEvent event = chatEvent("format", "%");
        listener.handleClanChat(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void handleClanChat_cancelsEvent_whenPlayerNotInClan() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(null);

        AsyncPlayerChatEvent event = chatEvent("format", "%hello");
        listener.handleClanChat(event);

        verify(event).setCancelled(true);
        verify(player).sendMessage(anyString());
    }

    @Test
    void handleClanChat_stripsPercentPrefix_andSetsFormat() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(clan);
        when(memberList.getListOfMembers(any())).thenReturn(new ArrayList<>());
        when(clan.getName()).thenReturn("Warriors");
        when(clan.hasLeader("alice")).thenReturn(false);
        when(clan.hasModer("alice")).thenReturn(false);

        AsyncPlayerChatEvent event = chatEvent("format", "%hello clan");
        listener.handleClanChat(event);

        verify(event).setMessage("hello clan");
        verify(event).setFormat(any());
    }

    @Test
    void handleClanChat_leaderGetsRedColor() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(clan);
        when(memberList.getListOfMembers(any())).thenReturn(new ArrayList<>());
        when(clan.getName()).thenReturn("Warriors");
        when(clan.hasLeader("alice")).thenReturn(true);

        AsyncPlayerChatEvent event = chatEvent("format", "%hi");

        // Capture the format set
        listener.handleClanChat(event);

        // Verify lang was called with the leader colour prefix
        verify(core).lang(anyString(), anyString(),
                contains(ChatColor.DARK_RED.toString()), anyString());
    }

    @Test
    void handleClanChat_moderatorGetsGreenColor() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(clan);
        when(memberList.getListOfMembers(any())).thenReturn(new ArrayList<>());
        when(clan.getName()).thenReturn("Warriors");
        when(clan.hasLeader("alice")).thenReturn(false);
        when(clan.hasModer("alice")).thenReturn(true);

        AsyncPlayerChatEvent event = chatEvent("format", "%hi");

        listener.handleClanChat(event);

        verify(core).lang(anyString(), anyString(),
                contains(ChatColor.GREEN.toString()), anyString());
    }

    @Test
    void handleClanChat_memberGetsYellowColor() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(clan);
        when(memberList.getListOfMembers(any())).thenReturn(new ArrayList<>());
        when(clan.getName()).thenReturn("Warriors");
        when(clan.hasLeader("alice")).thenReturn(false);
        when(clan.hasModer("alice")).thenReturn(false);

        AsyncPlayerChatEvent event = chatEvent("format", "%hi");

        listener.handleClanChat(event);

        verify(core).lang(anyString(), anyString(),
                contains(ChatColor.YELLOW.toString()), anyString());
    }

    @Test
    void handleClanChat_stripsSectionSign_fromMessage() {
        when(config.getBoolean("settings.clan_chat")).thenReturn(true);
        when(clanList.getClanByName("alice")).thenReturn(clan);
        when(memberList.getListOfMembers(any())).thenReturn(new ArrayList<>());
        when(clan.getName()).thenReturn("Warriors");
        when(clan.hasLeader("alice")).thenReturn(false);
        when(clan.hasModer("alice")).thenReturn(false);

        AsyncPlayerChatEvent event = chatEvent("format", "%hello §cred");
        listener.handleClanChat(event);

        verify(event).setMessage("hello &cred");
    }
}
