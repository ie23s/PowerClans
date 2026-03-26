package com.ie23s.bukkit.plugin.powerclans.modules.level.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/** Periodic task that increments {@link com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey#ONLINE_TIME} for each online clan member. */
public class PlayedTime implements Runnable {
    private final Core core;

    /** @param core the plugin core */
    public PlayedTime(Core core) {
        this.core = core;
    }

    @Override
    public void run() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        for (Player p :
                players) {
            if (core.getMemberList().isMember(p.getName())) {
                Clan clan = core.getClanList().getClanByName(p.getName());
                clan.update(ClanDataKey.ONLINE_TIME, clan.getInt(ClanDataKey.ONLINE_TIME) + 1);
            }
        }
    }
}
