package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.WorldGuardUtils;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

/**
 * Prevents friendly fire between clan members.
 *
 * <p>Damage between two players in the same clan is cancelled when the clan's
 * PvP protection flag ({@link com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey#PVP}) is {@code true}.
 * The check is skipped entirely when WorldGuard allows PvP at the victim's location
 * and the global {@code settings.pvp} config flag is also set.
 *
 * <p>Arrow and {@link ThrownPotion} projectiles are unwrapped to their shooter
 * before applying the clan check.
 */
public class CombatListener implements Listener {

    private final Core core;

    /**
     * Creates a CombatListener backed by the given plugin core.
     * @param core plugin core instance
     */
    public CombatListener(Core core) {
        this.core = core;
    }

    /**
     * Intercepts entity damage events and cancels friendly fire between clan members.
     *
     * @param event the damage event
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player damager = resolvePlayerDamager(event.getDamager());
        if (damager == null) return;

        handleFriendlyFire(event, victim, damager);
    }

    /**
     * Resolves the actual {@link Player} responsible for the damage.
     * {@link Arrow} and {@link ThrownPotion} entities are unwrapped to their shooter.
     *
     * @param rawDamager raw damager entity from the event
     * @return the player who caused the damage, or {@code null} if the source is not a player
     */
    private Player resolvePlayerDamager(Object rawDamager) {
        if (rawDamager instanceof Arrow arrow)           rawDamager = arrow.getShooter();
        else if (rawDamager instanceof ThrownPotion pot) rawDamager = pot.getShooter();
        return rawDamager instanceof Player p ? p : null;
    }

    /**
     * Cancels the damage event if both players are in the same clan and the clan's
     * PvP protection is active.
     *
     * @param event   the damage event to potentially cancel
     * @param victim  the player receiving the damage
     * @param damager the player dealing the damage
     */
    private void handleFriendlyFire(EntityDamageByEntityEvent event, Player victim, Player damager) {
        if (WorldGuardUtils.getFlag(victim.getLocation(), Flags.PVP)
                && core.getConfig().getBoolean("settings.pvp")) return;

        UUID damagerUuid = damager.getUniqueId();
        UUID victimUuid  = victim.getUniqueId();

        if (!core.getMemberList().isMemberByUuid(damagerUuid)) return;
        if (!core.getMemberList().isMemberByUuid(victimUuid))  return;

        Clan clan = core.getClanList().getClanByPlayerUuid(damagerUuid);
        if (clan == null) return;
        if (!clan.hasClanMember(victim)) return;
        if (victimUuid.equals(damagerUuid)) return;
        if (!clan.getBoolean(ClanDataKey.PVP)) return;

        damager.sendMessage(core.lang("other.damage_in_clan"));
        event.setCancelled(true);
    }
}
