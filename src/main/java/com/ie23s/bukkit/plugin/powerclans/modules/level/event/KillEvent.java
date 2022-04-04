package com.ie23s.bukkit.plugin.powerclans.modules.level.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

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
        System.out.println("kek1");
        if (((Monster) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
            return;
        System.out.println("kek2");
        if (!ml.isMember(player.getName()))
            return;
        System.out.println("kek3");

        core.getClanList().getClanByName(player.getName()).addMobKill();
    }

    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        String killed = e.getEntity().getName();
        String killer = Objects.requireNonNull(e.getEntity().getKiller()).getName();
        if (!ml.isMember(killed) || !ml.isMember(killer))
            return;
        if (core.getClanList().getClanByName(killer).getName().equalsIgnoreCase(
                core.getClanList().getClanByName(killed).getName()
        ))
            return;
        core.getClanList().getClanByName(killer).addPlayerKill();

    }
}

