package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Warm {

    @SuppressWarnings("rawtypes")
    private static HashMap players = new HashMap();
    @SuppressWarnings("rawtypes")
    private static HashMap playerloc = new HashMap();


    @SuppressWarnings("unchecked")
    public static void addPlayer(Player player, Clan clan) {
        if (player.hasPermission("PowerClans.warm.ignore")) {
            clan(player, clan);
        } else if (isWarming(player)) {
            player.sendMessage(Language.getMessage("other.warm_alredy"));
        } else {
            player.sendMessage(Language.getMessage("other.warm_use", Configuration.getConfiguration().getInt("settings.warm")));
            int taskIndex = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Warm.WarmTask(player, clan), (long) (Configuration.getConfiguration().getInt("settings.warm") * 20));
            players.put(player.getName(), taskIndex);
            playerloc.put(player.getName(), player.getLocation());
        }
    }

    private static boolean hasMoved(Player player) {
        Location curloc = player.getLocation();
        Location cmdloc = (Location) playerloc.get(player.getName());
        return cmdloc.distanceSquared(curloc) > 0.0D;
    }

    private static boolean isWarming(Player player) {
        return players.containsKey(player.getName());
    }

    public static void cancelWarming(Player player) {
        if (isWarming(player)) {
            Bukkit.getScheduler().cancelTask((Integer) players.get(player.getName()));
            players.remove(player.getName());
            playerloc.remove(player.getName());
            player.sendMessage(Language.getMessage("other.warm_canceled"));
        }

    }

    public static void clan(Player pl, Clan clan) {
        pl.teleport(clan.getHome());
        pl.sendMessage(Language.getMessage("clan.teleport"));
    }

    private static class WarmTask implements Runnable {

        private Player player;
        private Clan clan;


        WarmTask(Player player, Clan clan) {
            this.player = player;
            this.clan = clan;
        }

        public void run() {
            if (Warm.hasMoved(this.player)) {
                Warm.cancelWarming(this.player);
            } else {
                Warm.players.remove(this.player.getName());
                Warm.playerloc.remove(this.player.getName());
                Warm.clan(this.player, this.clan);
            }
        }
    }
}
