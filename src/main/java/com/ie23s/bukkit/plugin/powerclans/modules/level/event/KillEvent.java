package com.ie23s.bukkit.plugin.powerclans.modules.level.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class KillEvent implements Listener {
    private final Core core;

    private final MemberList ml;

    public KillEvent(Core core) {
        this.core = core;

        this.ml = core.getMemberList();
    }

    //    @EventHandler
//    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
//        Entity player = event.getDamager();
//        Entity entity = event.getEntity();
//        if (!(player instanceof Player) || !(entity instanceof Monster))
//            return;
//        if (((Monster) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
//            return;
//        if (!ml.isMember(player.getName()))
//            return;
//
//        core.getClanList().getClanByName(player.getName()).addMobKill();
//    }
    @EventHandler
    public void onKillMob(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null)
            return;
        if (!(e.getEntity() instanceof Monster))
            return;
        if (!ml.isMember(killer.getName()))
            return;

        core.getClanList().getClanByName(killer.getName()).addMobKill();
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

