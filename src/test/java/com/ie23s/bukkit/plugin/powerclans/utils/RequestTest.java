package com.ie23s.bukkit.plugin.powerclans.utils;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RequestTest {

    @Mock
    Player player;

    @Mock
    Player otherPlayer;

    @BeforeEach
    void clearRequests() {
        Request.requests.clear();
    }

    @Test
    void getTime_isBetweenBeforeAndAfterConstruction() {
        long before = System.currentTimeMillis();
        Request req = new Request(player, "sender", RequestType.CREATE, new String[]{"create"});
        long after = System.currentTimeMillis();

        assertTrue(req.getTime() >= before);
        assertTrue(req.getTime() <= after);
    }

    @Test
    void send_addsToGlobalList() {
        Request req = new Request(player, "sender", RequestType.INVITE, null);
        req.send();

        assertEquals(1, Request.requests.size());
        assertSame(req, Request.requests.getFirst());
    }

    @Test
    void send_replacesExistingRequestForSamePlayer() {
        Request req1 = new Request(player, "sender", RequestType.INVITE, null);
        Request req2 = new Request(player, "sender2", RequestType.CREATE, null);

        req1.send();
        req2.send();

        assertEquals(1, Request.requests.size());
        assertSame(req2, Request.requests.getFirst());
    }

    @Test
    void remove_removesFromGlobalList() {
        Request req = new Request(player, "sender", RequestType.INVITE, null);
        req.send();
        req.remove();

        assertTrue(Request.requests.isEmpty());
    }

    @Test
    void get_returnsMatchingRequestByPlayer() {
        Request req = new Request(player, "sender", RequestType.INVITE, null);
        req.send();

        assertSame(req, Request.get(player));
    }

    @Test
    void get_returnsNullWhenNoRequest() {
        assertNull(Request.get(player));
    }

    @Test
    void get_returnsNullAfterRemove() {
        Request req = new Request(player, "sender", RequestType.INVITE, null);
        req.send();
        req.remove();

        assertNull(Request.get(player));
    }

    @Test
    void get_returnsCorrectRequestAmongMultiple() {
        Request req1 = new Request(player, "sender1", RequestType.INVITE, null);
        Request req2 = new Request(otherPlayer, "sender2", RequestType.CREATE, null);

        req1.send();
        req2.send();

        assertSame(req1, Request.get(player));
        assertSame(req2, Request.get(otherPlayer));
    }
}
