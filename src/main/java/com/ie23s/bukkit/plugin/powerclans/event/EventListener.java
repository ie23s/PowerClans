package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles player lifecycle events for the PowerClans plugin.
 *
 * <p>Combat logic is handled by {@link CombatListener};
 * chat logic is handled by {@link ChatListener}.
 */
public class EventListener implements Listener {

    private final Core core;

    /**
     * @param core plugin core instance
     */
    public EventListener(Core core) {
        this.core = core;
    }

    /**
     * Removes the pending clan request for the kicked player, if one exists.
     *
     * @param event the kick event
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Request request = Request.get(event.getPlayer());
        if (request != null) request.remove();
    }

    /**
     * Cancels any active warming action when the player moves.
     *
     * @param event the move event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() != null && event.getFrom().distance(event.getTo()) > 0.0) {
            core.getUtils().getWarm().cancelWarming(event.getPlayer());
        }
    }
}
