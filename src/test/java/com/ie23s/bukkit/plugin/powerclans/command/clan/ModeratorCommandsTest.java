package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.command.CommandSender;
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
class ModeratorCommandsTest {

    @Mock
    Core core;

    @Mock
    Clan clan;

    @Mock
    CommandSender sender;

    @Mock
    ClanList clanList;

    @Mock
    MemberList memberList;

    @BeforeEach
    void setUp() {
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(sender.hasPermission(anyString())).thenReturn(true);
        lenient().when(sender.getName()).thenReturn("leader");
        lenient().when(clan.hasLeader("leader")).thenReturn(true);
        lenient().when(core.getClanList()).thenReturn(clanList);
        lenient().when(core.getMemberList()).thenReturn(memberList);
    }

    // -------------------------------------------------------------------------
    // AddModer.validate
    // -------------------------------------------------------------------------

    @Test
    void addmoder_validate_failsWhenTargetNotInClan() {
        when(clan.hasClanMember("outsider")).thenReturn(false);
        ModeratorCommands.AddModer cmd = new ModeratorCommands.AddModer(core);

        boolean result = cmd.validate(sender, new String[]{"addmoder", "outsider"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._14");
    }

    @Test
    void addmoder_validate_failsWhenTargetIsLeader() {
        when(clan.hasClanMember("target")).thenReturn(true);
        when(clan.hasLeader("target")).thenReturn(true);
        ModeratorCommands.AddModer cmd = new ModeratorCommands.AddModer(core);

        boolean result = cmd.validate(sender, new String[]{"addmoder", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._15");
    }

    @Test
    void addmoder_validate_failsWhenTargetAlreadyModer() {
        when(clan.hasClanMember("member")).thenReturn(true);
        when(clan.hasLeader("member")).thenReturn(false);
        when(clan.hasModer("member")).thenReturn(true);
        ModeratorCommands.AddModer cmd = new ModeratorCommands.AddModer(core);

        boolean result = cmd.validate(sender, new String[]{"addmoder", "member"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._16");
    }

    @Test
    void addmoder_validate_passesForValidPromotion() {
        when(clan.hasClanMember("member")).thenReturn(true);
        when(clan.hasLeader("member")).thenReturn(false);
        when(clan.hasModer("member")).thenReturn(false);
        ModeratorCommands.AddModer cmd = new ModeratorCommands.AddModer(core);

        boolean result = cmd.validate(sender, new String[]{"addmoder", "member"}, clan, "leader");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    // DelModer.validate
    // -------------------------------------------------------------------------

    @Test
    void delmoder_validate_failsWhenTargetNotInClan() {
        when(clan.hasClanMember("outsider")).thenReturn(false);
        ModeratorCommands.DelModer cmd = new ModeratorCommands.DelModer(core);

        boolean result = cmd.validate(sender, new String[]{"delmoder", "outsider"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._14");
    }

    @Test
    void delmoder_validate_failsWhenTargetIsNotModer() {
        when(clan.hasClanMember("member")).thenReturn(true);
        when(clan.hasLeader("member")).thenReturn(false);
        when(clan.hasModer("member")).thenReturn(false);
        ModeratorCommands.DelModer cmd = new ModeratorCommands.DelModer(core);

        boolean result = cmd.validate(sender, new String[]{"delmoder", "member"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._18");
    }

    @Test
    void delmoder_validate_passesForExistingModer() {
        when(clan.hasClanMember("moder")).thenReturn(true);
        when(clan.hasLeader("moder")).thenReturn(false);
        when(clan.hasModer("moder")).thenReturn(true);
        ModeratorCommands.DelModer cmd = new ModeratorCommands.DelModer(core);

        boolean result = cmd.validate(sender, new String[]{"delmoder", "moder"}, clan, "leader");

        assertTrue(result);
    }

    // -------------------------------------------------------------------------
    // Leader.validate
    // -------------------------------------------------------------------------

    @Test
    void leader_validate_failsWhenTargetNotAMember() {
        when(memberList.isMember("target")).thenReturn(false);
        ModeratorCommands.Leader cmd = new ModeratorCommands.Leader(core);

        boolean result = cmd.validate(sender, new String[]{"leader", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._33");
    }

    @Test
    void leader_validate_failsWhenTransferToSelf() {
        when(memberList.isMember("leader")).thenReturn(true);

        Clan targetClan = mock(Clan.class);
        when(core.getClanList().getClanByName("leader")).thenReturn(targetClan);
        when(targetClan.getName()).thenReturn("TestClan");
        when(clan.getName()).thenReturn("TestClan");

        ModeratorCommands.Leader cmd = new ModeratorCommands.Leader(core);

        boolean result = cmd.validate(sender, new String[]{"leader", "leader"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._35");
    }

    @Test
    void leader_validate_failsWhenTargetNotInSameClan() {
        when(memberList.isMember("target")).thenReturn(true);
        when(core.getClanList().getClanByName("target")).thenReturn(null);
        ModeratorCommands.Leader cmd = new ModeratorCommands.Leader(core);

        boolean result = cmd.validate(sender, new String[]{"leader", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._14");
    }

    @Test
    void leader_validate_failsWhenTargetInDifferentClan() {
        when(memberList.isMember("target")).thenReturn(true);

        Clan targetClan = mock(Clan.class);
        when(core.getClanList().getClanByName("target")).thenReturn(targetClan);
        when(targetClan.getName()).thenReturn("OtherClan");
        when(clan.getName()).thenReturn("TestClan");

        ModeratorCommands.Leader cmd = new ModeratorCommands.Leader(core);

        boolean result = cmd.validate(sender, new String[]{"leader", "target"}, clan, "leader");

        assertFalse(result);
        verify(sender).sendMessage("errors._34");
    }

    @Test
    void leader_validate_passesForValidTransfer() {
        when(memberList.isMember("target")).thenReturn(true);

        Clan targetClan = mock(Clan.class);
        when(core.getClanList().getClanByName("target")).thenReturn(targetClan);
        when(targetClan.getName()).thenReturn("TestClan");
        when(clan.getName()).thenReturn("TestClan");

        ModeratorCommands.Leader cmd = new ModeratorCommands.Leader(core);

        boolean result = cmd.validate(sender, new String[]{"leader", "target"}, clan, "leader");

        assertTrue(result);
    }
}
