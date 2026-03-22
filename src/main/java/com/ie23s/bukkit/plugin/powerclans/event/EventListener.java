package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.WorldGuardUtils;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Bukkit event listener for PowerClans plugin.
 * Handles clan-related game events: friendly fire prevention, clan chat, and clan tag substitution.
 */
public class EventListener implements Listener {
    private final Core core;
    /** Placeholder used in chat format strings to mark where the clan tag is injected. */
    private static final String CLANTAG = "!clantag!";

    /**
     * @param core plugin core instance
     */
    public EventListener(Core core) {
        this.core = core;
    }

    /**
     * Removes pending clan invites when a player is kicked.
     */
    @EventHandler
    public void playerKickEvent(PlayerKickEvent event) {
        if (Request.get(event.getPlayer()) != null) {
            Objects.requireNonNull(Request.get(event.getPlayer())).remove();
        }
    }

    /**
     * Cancels warming actions when a player moves.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void playerMoveEvent(PlayerMoveEvent event) {
        if (event.getTo() != null && event.getFrom().distance(event.getTo()) > 0.0D) {
            core.getUtils().getWarm().cancelWarming(event.getPlayer());
        }

    }

    /**
     * Prevents clan members from damaging each other when clan PvP is enabled.
     * Resolves the real attacker from projectiles (arrow, thrown potion) before checking.
     */
    @EventHandler
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player attacker)) return;

        Player damager = resolvePlayerDamager(event.getDamager());
        if (damager == null) return;

        handleClanFriendlyFire(event, attacker, damager);
    }

    /**
     * Resolves the actual player damager, unwrapping projectile sources if needed.
     *
     * @param damager raw damager from the event
     * @return the player who caused the damage, or {@code null} if not a player
     */
    private Player resolvePlayerDamager(Object damager) {
        if (damager instanceof Arrow arrow) damager = arrow.getShooter();
        else if (damager instanceof ThrownPotion thrownPotion) damager = thrownPotion.getShooter();

        return damager instanceof Player player ? player : null;
    }

    /**
     * Cancels the damage event if both players are in the same clan and clan PvP is enabled.
     *
     * @param event    the damage event to potentially cancel
     * @param attacker the player receiving damage
     * @param damager  the player dealing damage
     */
    private void handleClanFriendlyFire(EntityDamageByEntityEvent event, Player attacker, Player damager) {
        if (WorldGuardUtils.getFlag(attacker.getLocation(), Flags.PVP) && core.getConfig().getBoolean("settings.pvp")) return;

        Clan userClan = core.getClanList().getClanByName(damager.getName());
        if (userClan == null) return;

        if (!core.getMemberList().isMember(damager.getName())) return;
        if (!core.getMemberList().isMember(attacker.getName())) return;
        if (!userClan.hasClanMember(attacker.getName())) return;
        if (attacker.getName().equals(damager.getName())) return;

        if (!userClan.isPvp()) return;

        damager.sendMessage(core.lang("other.damage_in_clan"));
        event.setCancelled(true);
    }

    /**
     * Entry point for chat processing: substitutes the clan tag and handles clan-only chat.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void playerChatEvent(AsyncPlayerChatEvent event) {
        handleClanTag(event);
        handleClanChat(event);
    }

    /**
     * Replaces the {@value CLANTAG} placeholder in the chat format with the player's clan tag,
     * or removes it if the player has no clan.
     */
    private void handleClanTag(AsyncPlayerChatEvent event) {
        if (core.getMemberList().isMember(event.getPlayer().getName()) && event.getFormat().contains(CLANTAG)) {
            event.setFormat(event.getFormat().replace(CLANTAG, core.getClanList().getClanByName(event.getPlayer().getName()).getTag()));
        } else {
            event.setFormat(event.getFormat().replace(CLANTAG, ""));
        }
    }

    /**
     * Handles clan-only chat when the message starts with {@code %}.
     * Limits recipients to online clan members and formats the message with the sender's rank color.
     * Cancels the event if the player is not in a clan.
     */
    private void handleClanChat(AsyncPlayerChatEvent event) {
        if (!core.getConfig().getBoolean("settings.clan_chat")) return;
        if (!event.getMessage().startsWith("%") || event.getMessage().length() <= 1) return;

        Clan userClan = core.getClanList().getClanByName(event.getPlayer().getName());

        if (userClan == null) {
            event.getPlayer().sendMessage(core.lang("error._9"));
            event.setCancelled(true);
            return;
        }

        event.getRecipients().clear();
        ArrayList<String> var4 = core.getMemberList().getListOfMembers(userClan.getName());
        for (String name : var4) {
            @SuppressWarnings("deprecation") OfflinePlayer pl = Bukkit.getOfflinePlayer(name);

            if (pl.isOnline()) {
                event.getRecipients().add(pl.getPlayer());
            }
        }

        ChatColor c1 = ChatColor.YELLOW;

        if (userClan.hasModer(event.getPlayer().getName())) {
            c1 = ChatColor.GREEN;
        }

        if (userClan.hasLeader(event.getPlayer().getName())) {
            c1 = ChatColor.DARK_RED;
        }

        event.setFormat(core.lang("chat.clanchat", core.lang("chat.clan"), c1 + event.getPlayer().getName(), "%2$s"));
        event.setMessage(event.getMessage().substring(1).replace("§", "&"));
    }

    /**
     * Replaces the {@value CLANTAG} placeholder with the clan name (not tag) in the chat format.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void asyncPlayerChatTagEvent(AsyncPlayerChatEvent event) {

        if (core.getMemberList().isMember(event.getPlayer().getName()) && event.getFormat().contains(CLANTAG)) {
            event.setFormat(event.getFormat().replace(CLANTAG, Objects.requireNonNull(core.getClanList().getClanByName(event.getPlayer().getName())).getName()));
        } else {
            event.setFormat(event.getFormat().replace(CLANTAG, ""));
        }

    }
}
