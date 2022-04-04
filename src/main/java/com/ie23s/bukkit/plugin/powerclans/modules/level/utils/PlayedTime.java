package com.ie23s.bukkit.plugin.powerclans.modules.level.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PlayedTime implements Runnable {
    private final Core core;

    public PlayedTime(Core core) {
        this.core = core;
    }

    @Override
    public void run() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        for (Player p :
                players) {
            if (core.getMemberList().isMember(p.getDisplayName())) {
                core.getClanList().getClanByName(p.getDisplayName()).addOnlineTime();
            }
        }
    }
}
