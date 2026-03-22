package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class MemberCommandsTest {

    @Mock
    Core core;

    @Mock
    Clan clan;

    @Mock
    CommandSender sender;

    @Mock
    MemberList memberList;

    @Mock
    Player targetPlayer;

    @BeforeEach
    void setUp() {
        Request.requests.clear();
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), (Object[]) any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(sender.hasPermission(anyString())).thenReturn(true);
        lenient().when(sender.getName()).thenReturn("leader");
        lenient().when(clan.hasLeader("leader")).thenReturn(true);
        lenient().when(core.getMemberList()).thenReturn(memberList);
    }

    // -------------------------------------------------------------------------
    // Invite.validate
    // -------------------------------------------------------------------------

    @Test
    void invite_validate_failsWhenTargetOffline() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(null);
            MemberCommands.Invite cmd = new MemberCommands.Invite(core);

            boolean result = cmd.validate(sender, new String[]{"invite", "target"}, clan, "leader");

            assertFalse(result);
            verify(sender).sendMessage("errors._20");
        }
    }

    @Test
    void invite_validate_failsWhenTargetAlreadyMember() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(targetPlayer);
            when(memberList.isMember("target")).thenReturn(true);
            MemberCommands.Invite cmd = new MemberCommands.Invite(core);

            boolean result = cmd.validate(sender, new String[]{"invite", "target"}, clan, "leader");

            assertFalse(result);
            verify(sender).sendMessage("errors._21");
        }
    }

    @Test
    void invite_validate_failsWhenClanFull() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(targetPlayer);
            when(memberList.isMember("target")).thenReturn(false);
            when(clan.getMaxPlayers()).thenReturn(2);
            List<String> fullList = new ArrayList<>(List.of("member1", "member2"));
            when(memberList.getListOfMembers(clan.getName())).thenReturn(new ArrayList<>(fullList));
            MemberCommands.Invite cmd = new MemberCommands.Invite(core);

            boolean result = cmd.validate(sender, new String[]{"invite", "target"}, clan, "leader");

            assertFalse(result);
            verify(sender).sendMessage("errors._22");
        }
    }

    @Test
    void invite_validate_failsWhenTargetHasPendingRequest() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(targetPlayer);
            when(memberList.isMember("target")).thenReturn(false);
            when(clan.getMaxPlayers()).thenReturn(10);
            when(memberList.getListOfMembers(clan.getName())).thenReturn(new ArrayList<>());

            Request pendingRequest = new Request(clan, targetPlayer, "someone", RequestType.INVITE);
            pendingRequest.send();

            MemberCommands.Invite cmd = new MemberCommands.Invite(core);

            boolean result = cmd.validate(sender, new String[]{"invite", "target"}, clan, "leader");

            assertFalse(result);
            verify(sender).sendMessage("error.23");
        }
    }

    @Test
    void invite_validate_passesForValidInvite() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(targetPlayer);
            when(memberList.isMember("target")).thenReturn(false);
            when(clan.getMaxPlayers()).thenReturn(10);
            when(memberList.getListOfMembers(clan.getName())).thenReturn(new ArrayList<>());
            MemberCommands.Invite cmd = new MemberCommands.Invite(core);

            boolean result = cmd.validate(sender, new String[]{"invite", "target"}, clan, "leader");

            assertTrue(result);
        }
    }

    // -------------------------------------------------------------------------
    // Kick.validate
    // -------------------------------------------------------------------------

    @Test
    void kick_validate_failsWhenSenderIsNeitherLeaderNorModer() {
        when(clan.hasLeader("leader")).thenReturn(false);
        when(clan.hasModer("leader")).thenReturn(false);
        MemberCommands.Kick cmd = new MemberCommands.Kick(core);

        boolean result = cmd.validate(sender, new String[]{"kick", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._24");
    }

    @Test
    void kick_validate_failsWhenNoPermission() {
        when(sender.hasPermission("PowerClans.kick")).thenReturn(false);
        MemberCommands.Kick cmd = new MemberCommands.Kick(core);

        boolean result = cmd.validate(sender, new String[]{"kick", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._1");
    }

    @Test
    void kick_validate_failsWhenNotInClan() {
        MemberCommands.Kick cmd = new MemberCommands.Kick(core);

        boolean result = cmd.validate(sender, new String[]{"kick", "target"}, null, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void kick_validate_failsWhenTargetNotClanMember() {
        when(clan.hasModer("leader")).thenReturn(false);
        when(clan.hasClanMember("target")).thenReturn(false);
        MemberCommands.Kick cmd = new MemberCommands.Kick(core);

        boolean result = cmd.validate(sender, new String[]{"kick", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._14");
    }

    @Test
    void kick_validate_failsWhenTryingToKickLeader() {
        when(clan.hasModer("leader")).thenReturn(false);
        when(clan.hasClanMember("target")).thenReturn(true);
        when(clan.getLeader()).thenReturn("target");
        MemberCommands.Kick cmd = new MemberCommands.Kick(core);

        boolean result = cmd.validate(sender, new String[]{"kick", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._25");
    }

    @Test
    void kick_validate_passesForValidKick() {
        when(clan.hasModer("leader")).thenReturn(false);
        when(clan.hasClanMember("member")).thenReturn(true);
        when(clan.getLeader()).thenReturn("leader");
        MemberCommands.Kick cmd = new MemberCommands.Kick(core);

        boolean result = cmd.validate(sender, new String[]{"kick", "member"}, clan, "leader");

        assertTrue(result);
    }
}
