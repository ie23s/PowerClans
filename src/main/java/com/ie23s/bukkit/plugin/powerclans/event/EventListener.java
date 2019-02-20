package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.Warm;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
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

@SuppressWarnings({"ConstantConditions", "unused"})
public class EventListener implements Listener {

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
            Warm.cancelWarming(event.getPlayer());
        }

    }

    @SuppressWarnings("deprecation")
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
                Clan userClan = Clan.getClanByName(damager.getName());

                ApplicableRegionSet set = Main.getWG().getRegionManager(attacker.getWorld()).getApplicableRegions(attacker.getLocation());

                if (set.getFlag(DefaultFlag.PVP) == State.ALLOW && Configuration.getConfiguration().getBoolean("settings.pvp")) {
                    return;
                }

                if (Member.isMember(damager.getName()) && Member.isMember(attacker.getName()) && userClan.hasClanMember(attacker.getName()) && !attacker.getName().equals(damager.getName())) {
                    if (!userClan.isPvP()) {
                        return;
                    }
                    damager.sendMessage(Language.getMessage("other.damage_in_clan"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )

    public void PlayerChatEvent(AsyncPlayerChatEvent event) {

        if (Member.isMember(event.getPlayer().getName()) && event.getFormat().contains("!clantag!")) {
            event.setFormat(event.getFormat().replace("!clantag!", Clan.getClanByName(event.getPlayer().getName()).getTag()));
        } else {
            event.setFormat(event.getFormat().replace("!clantag!", ""));
        }
        if (Configuration.getConfiguration().getBoolean("settings.clan_chat")) {
            if (event.getMessage().startsWith("%") && event.getMessage().length() > 1) {
                Clan userClan = Clan.getClanByName(event.getPlayer().getName());

                if (userClan == null) {
                    event.getPlayer().sendMessage(Language.getMessage("error._9"));
                    event.setCancelled(true);
                    return;
                }

                event.getRecipients().clear();
                ArrayList<String> var4 = Member.getListOfMembers(Clan.getClanByName(event.getPlayer().getName()).getName());
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

                event.setFormat(Language.getMessage("chat.clanchat", Language.getMessage("chat.clan"), c1 + event.getPlayer().getName(), "%2$s"));
                event.setMessage(event.getMessage().substring(1).replace("§", "&"));
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void AsyncPlayerChatTagEvent(AsyncPlayerChatEvent event) {

        if (Member.isMember(event.getPlayer().getName()) && event.getFormat().contains("!clantag!")) {
            event.setFormat(event.getFormat().replace("!clantag!", Objects.requireNonNull(Clan.getClanByName(event.getPlayer().getName())).getName()));
        } else {
            event.setFormat(event.getFormat().replace("!clantag!", ""));
        }

    }
}
