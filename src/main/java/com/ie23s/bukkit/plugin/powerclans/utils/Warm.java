package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Manages the warmup delay before teleporting a player to their clan home.
 * If the player moves during the countdown, teleportation is cancelled.
 */
public class Warm {

    private final Core core;
    private final HashMap<String, Integer> players = new HashMap<>();
    private final HashMap<String, Location> playerloc = new HashMap<>();

    /** @param core the plugin core */
    public Warm(Core core) {
        this.core = core;
    }

    /**
     * Starts the warmup for {@code player} or teleports them immediately if they have bypass permission.
     *
     * @param player the player requesting teleportation
     * @param clan   the clan whose home is the destination
     */
    public void addPlayer(Player player, Clan clan) {
        if (player.hasPermission("PowerClans.warm.ignore")) {
            clan(player, clan);
        } else if (isWarming(player)) {
            player.sendMessage(core.lang("other.warm_alredy"));
        } else {
            player.sendMessage(core.lang("other.warm_use", core.getConfig().getInt("settings.warm")));
            int taskIndex = Bukkit.getScheduler().scheduleSyncDelayedTask(
                    core, new WarmTask(core, player, clan),
                    core.getConfig().getInt("settings.warm") * 20L);
            players.put(player.getName(), taskIndex);
            playerloc.put(player.getName(), player.getLocation());
        }
    }

    /**
     * Cancels an active warmup for {@code player} and notifies them.
     * No-op if the player is not currently warming up.
     *
     * @param player the player whose warmup should be cancelled
     */
    public void cancelWarming(Player player) {
        if (isWarming(player)) {
            Bukkit.getScheduler().cancelTask(players.get(player.getName()));
            players.remove(player.getName());
            playerloc.remove(player.getName());
            player.sendMessage(core.lang("other.warm_canceled"));
        }
    }

    /**
     * Teleports {@code pl} to the clan home immediately, without a warmup delay.
     *
     * @param pl   the player to teleport
     * @param clan the clan whose home is the destination
     */
    public void clan(Player pl, Clan clan) {
        pl.teleport(LocationSerializer.deserialize(clan.getString(ClanDataKey.HOME)));
        pl.sendMessage(core.lang("clan.teleport"));
    }

    private boolean hasMoved(Player player) {
        Location curloc = player.getLocation();
        Location cmdloc = playerloc.get(player.getName());
        return cmdloc.distanceSquared(curloc) > 0.0D;
    }

    private boolean isWarming(Player player) {
        return players.containsKey(player.getName());
    }

    private class WarmTask implements Runnable {

        private final Core core;
        private final Player player;
        private final Clan clan;

        WarmTask(Core core, Player player, Clan clan) {
            this.core = core;
            this.player = player;
            this.clan = clan;
        }

        @Override
        public void run() {
            if (core.getUtils().getWarm().hasMoved(player)) {
                core.getUtils().getWarm().cancelWarming(player);
            } else {
                players.remove(player.getName());
                playerloc.remove(player.getName());
                core.getUtils().getWarm().clan(player, clan);
            }
        }
    }
}
