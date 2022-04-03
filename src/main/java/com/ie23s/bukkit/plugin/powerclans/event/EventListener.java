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

public class EventListener implements Listener {
    private final Core core;

    public EventListener(Core core) {
        this.core = core;
    }

    @EventHandler
    public void PlayerKickEvent(PlayerKickEvent event) {
        if (Request.get(event.getPlayer()) != null) {
            Request.get(event.getPlayer()).remove();
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void PlayerMoveEvent(PlayerMoveEvent event) {
        if (event.getFrom().distance(event.getTo()) > 0.0D) {
            core.getUtils().getWarm().cancelWarming(event.getPlayer());
        }

    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Object d = event.getDamager();

            if (d instanceof Arrow) {
                d = ((Arrow) d).getShooter();
            } else if (d instanceof ThrownPotion) {
                d = ((ThrownPotion) d).getShooter();
            }

            if (d instanceof Player) {
                Player damager = (Player) d;
                Player attacker = (Player) event.getEntity();
                Clan userClan = core.getClanList().getClanByName(damager.getName());


                if (WorldGuardUtils.getFlag(attacker.getLocation(), Flags.PVP) && core.getConfig().getBoolean("settings.pvp")) {
                    return;
                }

                if (core.getMemberList().isMember(damager.getName()) && core.getMemberList().isMember(attacker.getName()) && userClan.hasClanMember(attacker.getName()) && !attacker.getName().equals(damager.getName())) {
                    if (!userClan.isPvP()) {
                        return;
                    }
                    damager.sendMessage(core.getLang().getMessage("other.damage_in_clan"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )

    public void PlayerChatEvent(AsyncPlayerChatEvent event) {

        if (core.getMemberList().isMember(event.getPlayer().getName()) && event.getFormat().contains("!clantag!")) {
            event.setFormat(event.getFormat().replace("!clantag!", core.getClanList().getClanByName(event.getPlayer().getName()).getTag()));
        } else {
            event.setFormat(event.getFormat().replace("!clantag!", ""));
        }
        if (core.getConfig().getBoolean("settings.clan_chat")) {
            if (event.getMessage().startsWith("%") && event.getMessage().length() > 1) {
                Clan userClan = core.getClanList().getClanByName(event.getPlayer().getName());

                if (userClan == null) {
                    event.getPlayer().sendMessage(core.getLang().getMessage("error._9"));
                    event.setCancelled(true);
                    return;
                }

                event.getRecipients().clear();
                ArrayList<String> var4 = core.getMemberList().getListOfMembers(core.getClanList().getClanByName(event.getPlayer().getName()).getName());
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

                event.setFormat(core.getLang().getMessage("chat.clanchat", core.getLang().getMessage("chat.clan"), c1 + event.getPlayer().getName(), "%2$s"));
                event.setMessage(event.getMessage().substring(1).replace("§", "&"));
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void AsyncPlayerChatTagEvent(AsyncPlayerChatEvent event) {

        if (core.getMemberList().isMember(event.getPlayer().getName()) && event.getFormat().contains("!clantag!")) {
            event.setFormat(event.getFormat().replace("!clantag!", Objects.requireNonNull(core.getClanList().getClanByName(event.getPlayer().getName())).getName()));
        } else {
            event.setFormat(event.getFormat().replace("!clantag!", ""));
        }

    }
}
