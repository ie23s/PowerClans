package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Warm {
    private final Core core;
    private final HashMap<String, Integer> players = new HashMap<>();
    private final HashMap<String, Location> playerloc = new HashMap<>();

    public Warm(Core core) {
        this.core = core;
    }

    @SuppressWarnings("unchecked")
    public void addPlayer(Player player, Clan clan) {
        if (player.hasPermission("PowerClans.warm.ignore")) {
            clan(player, clan);
        } else if (isWarming(player)) {
            player.sendMessage(core.getLang().getMessage("other.warm_alredy"));
        } else {
            player.sendMessage(core.getLang().getMessage("other.warm_use", core.getConfig().getInt("settings.warm")));
            int taskIndex = Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Warm.WarmTask(core, player, clan), core.getConfig().getInt("settings.warm") * 20);
            players.put(player.getName(), taskIndex);
            playerloc.put(player.getName(), player.getLocation());
        }
    }

    private boolean hasMoved(Player player) {
        Location curloc = player.getLocation();
        Location cmdloc = playerloc.get(player.getName());
        return cmdloc.distanceSquared(curloc) > 0.0D;
    }

    private boolean isWarming(Player player) {
        return players.containsKey(player.getName());
    }

    public void cancelWarming(Player player) {
        if (isWarming(player)) {
            Bukkit.getScheduler().cancelTask(players.get(player.getName()));
            players.remove(player.getName());
            playerloc.remove(player.getName());
            player.sendMessage(core.getLang().getMessage("other.warm_canceled"));
        }

    }

    public void clan(Player pl, Clan clan) {
        pl.teleport(clan.getHome());
        pl.sendMessage(core.getLang().getMessage("clan.teleport"));
    }

    private static class WarmTask implements Runnable {
        private final Core core;

        private final Player player;
        private final Clan clan;


        WarmTask(Core core, Player player, Clan clan) {
            this.core = core;
            this.player = player;
            this.clan = clan;
        }

        public void run() {
            if (core.getUtils().getWarm().hasMoved(this.player)) {
                core.getUtils().getWarm().cancelWarming(this.player);
            } else {
                core.getUtils().getWarm().players.remove(this.player.getName());
                core.getUtils().getWarm().playerloc.remove(this.player.getName());
                core.getUtils().getWarm().clan(this.player, this.clan);
            }
        }
    }
}
