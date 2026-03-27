package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class BaseClanCommandTest {

    static final UUID LEADER_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID MODER_UUID  = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    static final UUID MEMBER_UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Mock Core core;
    @Mock Clan clan;
    @Mock Player sender;
    @Mock MemberList memberList;

    BaseClanCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.getMemberList()).thenReturn(memberList);

        command = new BaseClanCommand(core) {
            @Override
            public boolean validate(org.bukkit.command.CommandSender s, String[] args, Clan c, String user) { return true; }

            @Override
            public void execute(org.bukkit.command.CommandSender s, String[] args, Clan c, String user) { /* test stub — no behaviour needed */ }
        };
    }

    @Test
    void perm_returnsFalseAndSendsMessageWhenNoPermission() {
        when(sender.hasPermission("PowerClans.create")).thenReturn(false);

        boolean result = command.perm(sender, new String[]{"create"});

        assertFalse(result);
        verify(sender).sendMessage("errors._1");
    }

    @Test
    void inClan_returnsFalseAndSendsMessageWhenNoClan() {
        boolean result = command.inClan(sender, null);

        assertFalse(result);
        verify(sender).sendMessage("errors._9");
    }

    @Test
    void hasTarget_returnsFalseAndSendsMessageWhenOnlySubcommand() {
        boolean result = command.hasTarget(sender, new String[]{"invite"});

        assertFalse(result);
        verify(sender).sendMessage("errors._12");
    }

    @Test
    void isLeader_returnsFalseAndSendsMessageForNonLeader() {
        when(sender.getUniqueId()).thenReturn(MEMBER_UUID);
        when(clan.hasLeader(MEMBER_UUID)).thenReturn(false);

        boolean result = command.isLeader(sender, clan, "errors._10");

        assertFalse(result);
        verify(sender).sendMessage("errors._10");
    }

    @Test
    void isLeaderOrModer_returnsTrueForLeader() {
        when(sender.getUniqueId()).thenReturn(LEADER_UUID);
        when(clan.hasLeader(LEADER_UUID)).thenReturn(true);

        boolean result = command.isLeaderOrModer(sender, clan, "errors._19");

        assertTrue(result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void isLeaderOrModer_returnsTrueForModer() {
        when(sender.getUniqueId()).thenReturn(MODER_UUID);
        when(clan.hasLeader(MODER_UUID)).thenReturn(false);
        when(clan.hasModer(MODER_UUID)).thenReturn(true);

        boolean result = command.isLeaderOrModer(sender, clan, "errors._19");

        assertTrue(result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void isLeaderOrModer_returnsFalseForMember() {
        when(sender.getUniqueId()).thenReturn(MEMBER_UUID);
        when(clan.hasLeader(MEMBER_UUID)).thenReturn(false);
        when(clan.hasModer(MEMBER_UUID)).thenReturn(false);

        boolean result = command.isLeaderOrModer(sender, clan, "errors._19");

        assertFalse(result);
        verify(sender).sendMessage("errors._19");
    }

    @Test
    void isClanMember_returnsFalseAndSendsMessageForNonMember() {
        when(memberList.getMember("outsider")).thenReturn(null);

        boolean result = command.isClanMember(sender, clan, "outsider");

        assertFalse(result);
        verify(sender).sendMessage("errors._14");
    }

    @Test
    void parsePositiveInt_parsesZero() {
        int result = command.parsePositiveInt(sender, "0", "errors.nan");

        assertEquals(0, result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void parsePositiveInt_returnsNegativeOneForNonNumericString() {
        int result = command.parsePositiveInt(sender, "abc", "errors.nan");

        assertEquals(-1, result);
        verify(sender).sendMessage("errors.nan");
    }

    @Test
    void parsePositiveInt_returnsNegativeOneForNegativeNumber() {
        int result = command.parsePositiveInt(sender, "-5", "errors.nan");

        assertEquals(-1, result);
        verify(sender).sendMessage("errors.nan");
    }
}
