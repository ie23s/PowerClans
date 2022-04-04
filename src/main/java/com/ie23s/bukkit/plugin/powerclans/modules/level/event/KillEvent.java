package com.ie23s.bukkit.plugin.powerclans.modules.level.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class KillEvent implements Listener {
    private final Core core;

    private final MemberList ml;

    public KillEvent(Core core) {
        this.core = core;

        this.ml = core.getMemberList();
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity player = event.getDamager();
        Entity entity = event.getEntity();
        if (!(player instanceof Player) || !(entity instanceof Monster))
            return;
        if (!entity.isDead())
            return;
        if (!ml.isMember(player.getName()))
            return;

        System.out.println("kek");
        core.getClanList().getClanByName(player.getName()).addMobKill();
    }

    @EventHandler
    public void PlayerDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity player = event.getDamager();
        Entity entity = event.getEntity();
        if (!(player instanceof Player) || !(entity instanceof Player))
            return;
        if (!entity.isDead())
            return;
        if (!ml.isMember(player.getName()) || !ml.isMember(entity.getName()))
            return;
        if (core.getClanList().getClanByName(player.getName()).getName().equalsIgnoreCase(
                core.getClanList().getClanByName(entity.getName()).getName()
        ))
            return;
        core.getClanList().getClanByName(player.getName()).addPlayerKill();
    }

}

