package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    Core core;

    @Mock
    Clan clan;

    @Mock
    CommandSender sender;

    BaseClanCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(core.lang(anyString())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(core.lang(anyString(), (Object[]) any())).thenAnswer(inv -> inv.getArgument(0));

        command = new BaseClanCommand(core) {
            @Override
            public boolean validate(CommandSender s, String[] args, Clan c, String user) {
                return true;
            }

            @Override
            public void execute(CommandSender s, String[] args, Clan c, String user) {
            }
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
        when(sender.getName()).thenReturn("member");
        when(clan.hasLeader("member")).thenReturn(false);

        boolean result = command.isLeader(sender, clan, "errors._10");

        assertFalse(result);
        verify(sender).sendMessage("errors._10");
    }

    @Test
    void isLeaderOrModer_returnsTrueForLeader() {
        when(sender.getName()).thenReturn("leader");
        when(clan.hasLeader("leader")).thenReturn(true);

        boolean result = command.isLeaderOrModer(sender, clan, "errors._19");

        assertTrue(result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void isLeaderOrModer_returnsTrueForModer() {
        when(sender.getName()).thenReturn("moder");
        when(clan.hasLeader("moder")).thenReturn(false);
        when(clan.hasModer("moder")).thenReturn(true);

        boolean result = command.isLeaderOrModer(sender, clan, "errors._19");

        assertTrue(result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void isLeaderOrModer_returnsFalseForMember() {
        when(sender.getName()).thenReturn("member");
        when(clan.hasLeader("member")).thenReturn(false);
        when(clan.hasModer("member")).thenReturn(false);

        boolean result = command.isLeaderOrModer(sender, clan, "errors._19");

        assertFalse(result);
        verify(sender).sendMessage("errors._19");
    }

    @Test
    void isClanMember_returnsFalseAndSendsMessageForNonMember() {
        when(clan.hasClanMember("outsider")).thenReturn(false);

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
