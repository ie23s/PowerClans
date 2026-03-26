package com.ie23s.bukkit.plugin.powerclans.modules.level.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

/** Listens for mob and player kill events and increments the relevant clan kill counters. */
public class KillEvent implements Listener {

    private final Core core;
    private final MemberList ml;

    /** @param core the plugin core */
    public KillEvent(Core core) {
        this.core = core;
        this.ml = core.getMemberList();
    }

    /** Increments {@link ClanDataKey#MOB_KILLS} when a clan member kills a monster. */
    @EventHandler
    public void onKillMob(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;
        if (!(e.getEntity() instanceof Monster)) return;
        if (!ml.isMember(killer.getName())) return;

        Clan clan = core.getClanList().getClanByName(killer.getName());
        clan.update(ClanDataKey.MOB_KILLS, clan.getInt(ClanDataKey.MOB_KILLS) + 1);
    }

    /** Increments {@link ClanDataKey#PLAYER_KILLS} when a clan member kills a player from another clan. */
    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        String killed = e.getEntity().getName();
        String killer = Objects.requireNonNull(e.getEntity().getKiller()).getName();
        if (!ml.isMember(killed) || !ml.isMember(killer)) return;
        Clan killerClan = core.getClanList().getClanByName(killer);
        if (killerClan.getName().equalsIgnoreCase(core.getClanList().getClanByName(killed).getName())) return;
        killerClan.update(ClanDataKey.PLAYER_KILLS, killerClan.getInt(ClanDataKey.PLAYER_KILLS) + 1);
    }
}
