package com.ie23s.bukkit.plugin.powerclans.event.level;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MobKillEvent implements Listener {
    private final Core core;

    public MobKillEvent(Core core) {
        this.core = core;
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity player = event.getDamager();
        Entity entity = event.getEntity();
        if (!(player instanceof Player) || !(entity instanceof Monster))
            return;
        if (!entity.isDead())
            return;
        core.getClanList().getClanByName(((Player) player).getDisplayName()); //TODO add killed mob
    }

}

