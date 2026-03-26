package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;
import java.util.UUID;
/**
 * Handles clan-related chat features.
 *
 * <p>Two features are provided:
 * <ol>
 *   <li><b>Clan-tag substitution</b> — the {@value CLANTAG_PLACEHOLDER} placeholder in the
 *       server's chat format string is replaced with the player's clan tag (e.g. {@code WAR}),
 *       or removed if the player has no clan.</li>
 *   <li><b>Clan-only chat</b> — messages prefixed with {@code %} are routed exclusively to
 *       online members of the sender's clan and formatted with a rank-based colour.
 *       The leading {@code %} is stripped and {@code §} is replaced with {@code &} to
 *       prevent players from injecting colour codes.
 *       The event is cancelled if the player is not in a clan.</li>
 * </ol>
 */
public class ChatListener implements Listener {

    /** Placeholder in chat format strings that is replaced with the player's clan tag. */
    static final String CLANTAG_PLACEHOLDER = "!clantag!";

    private final Core core;

    /**
     * @param core plugin core instance
     */
    public ChatListener(Core core) {
        this.core = core;
    }

    /**
     * Entry point: substitutes the clan-tag placeholder then processes clan-only chat.
     *
     * @param event the async chat event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        substituteClanTag(event);
        handleClanChat(event);
    }

    /**
     * Replaces {@value CLANTAG_PLACEHOLDER} in the event's format string with the
     * player's clan tag, or with an empty string if the player has no clan.
     *
     * @param event the chat event
     */
    void substituteClanTag(AsyncPlayerChatEvent event) {
        if (!event.getFormat().contains(CLANTAG_PLACEHOLDER)) return;
        UUID playerUuid = event.getPlayer().getUniqueId();
        if (core.getMemberList().isMemberByUuid(playerUuid)) {
            Clan clan = Objects.requireNonNull(
                    core.getClanList().getClanByPlayerUuid(playerUuid));
            event.setFormat(event.getFormat().replace(CLANTAG_PLACEHOLDER, clan.getString(ClanDataKey.TAG)));
        } else {
            event.setFormat(event.getFormat().replace(CLANTAG_PLACEHOLDER, ""));
        }
    }

    /**
     * Routes messages starting with {@code %} to online clan members only.
     *
     * <p>The sender's name is coloured by rank:
     * <ul>
     *   <li>{@link ChatColor#DARK_RED} — clan leader</li>
     *   <li>{@link ChatColor#GREEN} — moderator</li>
     *   <li>{@link ChatColor#YELLOW} — regular member</li>
     * </ul>
     *
     * @param event the chat event
     */
    void handleClanChat(AsyncPlayerChatEvent event) {
        if (!core.getConfig().getBoolean("settings.clan_chat")) return;
        String message = event.getMessage();
        if (!message.startsWith("%") || message.length() <= 1) return;

        UUID playerUuid = event.getPlayer().getUniqueId();
        Clan clan = core.getClanList().getClanByPlayerUuid(playerUuid);
        if (clan == null) {
            event.getPlayer().sendMessage(core.lang("error._9"));
            event.setCancelled(true);
            return;
        }

        event.getRecipients().clear();
        for (com.ie23s.bukkit.plugin.powerclans.clan.Member m : core.getMemberList().getListOfMembers(clan.getName())) {
            Player pl = Bukkit.getPlayer(m.getPlayerUuid());
            if (pl != null) {
                event.getRecipients().add(pl);
            }
        }

        event.setFormat(core.lang("chat.clanchat",
                core.lang("chat.clan"),
                rankColor(event.getPlayer().getName(), clan) + event.getPlayer().getName(),
                "%2$s"));
        event.setMessage(message.substring(1).replace("§", "&"));
    }

    /**
     * Returns the rank-based {@link ChatColor} for the given player within the given clan.
     *
     * @param playerName player whose rank to resolve
     * @param clan       the clan context
     * @return {@link ChatColor#DARK_RED} for leader, {@link ChatColor#GREEN} for moderator,
     *         {@link ChatColor#YELLOW} for a regular member
     */
    private ChatColor rankColor(String playerName, Clan clan) {
        if (clan.hasLeader(playerName)) return ChatColor.DARK_RED;
        if (clan.hasModer(playerName))  return ChatColor.GREEN;
        return ChatColor.YELLOW;
    }
}
