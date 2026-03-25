package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import com.ie23s.bukkit.plugin.powerclans.utils.Utils;
import com.ie23s.bukkit.plugin.powerclans.utils.Warm;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class EventListenerTest {

    @Mock Core core;
    @Mock Utils utils;
    @Mock Warm warm;
    @Mock Player player;

    EventListener listener;

    @BeforeEach
    void setUp() {
        Request.requests.clear();
        when(core.getUtils()).thenReturn(utils);
        when(utils.getWarm()).thenReturn(warm);
        listener = new EventListener(core);
    }

    // ── PlayerKickEvent ───────────────────────────────────────────────────────

    @Test
    void onPlayerKick_removesExistingRequest() {
        Request request = new Request(player, "sender", RequestType.INVITE, new String[0]);
        request.send();
        assertEquals(1, Request.requests.size());

        PlayerKickEvent event = mock(PlayerKickEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerKick(event);

        assertTrue(Request.requests.isEmpty());
    }

    @Test
    void onPlayerKick_noRequest_doesNotThrow() {
        PlayerKickEvent event = mock(PlayerKickEvent.class);
        when(event.getPlayer()).thenReturn(player);

        assertDoesNotThrow(() -> listener.onPlayerKick(event));
    }

    // ── PlayerMoveEvent ───────────────────────────────────────────────────────

    @Test
    void onPlayerMove_cancelsWarmingWhenPlayerMoved() {
        Location from = mock(Location.class);
        Location to   = mock(Location.class);
        when(from.distance(to)).thenReturn(1.0);

        PlayerMoveEvent event = mock(PlayerMoveEvent.class);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerMove(event);

        verify(warm).cancelWarming(player);
    }

    @Test
    void onPlayerMove_doesNotCancelWhenPositionUnchanged() {
        Location from = mock(Location.class);
        Location to   = mock(Location.class);
        when(from.distance(to)).thenReturn(0.0);

        PlayerMoveEvent event = mock(PlayerMoveEvent.class);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);

        listener.onPlayerMove(event);

        verify(warm, never()).cancelWarming(any());
    }

    @Test
    void onPlayerMove_doesNotCancelWhenToIsNull() {
        PlayerMoveEvent event = mock(PlayerMoveEvent.class);
        when(event.getTo()).thenReturn(null);
        when(event.getFrom()).thenReturn(mock(Location.class));

        listener.onPlayerMove(event);

        verify(warm, never()).cancelWarming(any());
    }
}
