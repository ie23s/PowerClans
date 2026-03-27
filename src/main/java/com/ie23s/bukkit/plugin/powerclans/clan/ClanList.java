package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory registry of all active {@link Clan} objects, keyed by lower-case clan name.
 *
 * <p>Acts as the authoritative source of clan identity at runtime. The map is populated at
 * startup by {@link com.ie23s.bukkit.plugin.powerclans.database.service.ClanService#loadAll()}
 * and kept up-to-date by {@link #create}.
 *
 * <p>This class is also the single point responsible for <b>member lifecycle</b>: adding,
 * removing, promoting/demoting moderators, broadcasting to members, and disbanding clans.
 * All such operations coordinate both the in-memory {@link MemberList} and the async
 * persistence layer.
 */
public class ClanList {

    private final HashMap<String, Clan> clans = new HashMap<>();
    private final Core core;

    /**
     * Creates an empty ClanList backed by the given plugin core.
     *
     * @param core plugin core used to reach other registries
     */
    public ClanList(Core core) {
        this.core = core;
    }

    // ── Clan registry queries ─────────────────────────────────────────────────

    /**
     * Returns the clan with the given name, or {@code null} if not found (case-insensitive).
     *
     * @param clan clan display name
     * @return the matching {@link Clan}, or {@code null}
     */
    public Clan getClan(String clan) {
        return clans.get(clan.toLowerCase());
    }

    /**
     * Returns the clan with the given UUID, or {@code null} if not found.
     *
     * @param uuid clan UUID
     * @return the matching {@link Clan}, or {@code null}
     */
    public Clan getClanByUuid(String uuid) {
        for (Clan clan : clans.values()) {
            if (clan.getUuid().equals(uuid)) return clan;
        }
        return null;
    }

    /**
     * Returns the clan of the player identified by their UUID, or {@code null} if not a member.
     * Prefer this overload in event handlers where a {@link Player} is available.
     *
     * @param playerUuid Mojang UUID of the player
     * @return the player's clan, or {@code null}
     */
    public Clan getClanByPlayerUuid(UUID playerUuid) {
        Member member = core.getMemberList().getMemberByUuid(playerUuid);
        return member != null ? getClan(member.getClan()) : null;
    }

    /**
     * Returns the clan of the player identified by their name, or {@code null} if not a member.
     * Use in command handlers where only a player name is available.
     *
     * @param playerName player name (any case)
     * @return the player's clan, or {@code null}
     */
    public Clan getClanByName(String playerName) {
        Member member = core.getMemberList().getMember(playerName);
        return member != null ? getClan(member.getClan()) : null;
    }

    /**
     * Returns the total number of registered clans.
     *
     * @return the clan count
     */
    public int number() {
        return clans.size();
    }

    /**
     * Returns the raw clan map (lower-case name → {@link Clan}).
     * Intended for bulk operations such as loading from DB or iteration.
     *
     * @return the mutable clan map
     */
    public Map<String, Clan> getClans() {
        return clans;
    }

    // ── Clan lifecycle ────────────────────────────────────────────────────────

    /**
     * Creates a new clan, registers it in memory, and asynchronously persists the clan row,
     * clan_data rows, and the leader's member row.
     * The clan UUID is generated in Java via {@link UUID#randomUUID()}.
     *
     * @param clan   raw clan name, may contain colour codes (stripped before storage)
     * @param leader the online founding leader player (name and UUID are taken from this object)
     * @return the newly created {@link Clan}
     */
    public Clan create(String clan, Player leader) {
        String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', clan));
        Clan c = new Clan(core, 0, UUID.randomUUID().toString(), strippedName,
                leader.getUniqueId(), core.getConfig().getInt("settings.default_max"), 1);
        c.set(ClanDataKey.TAG, clan);
        clans.put(strippedName.toLowerCase(), c);
        core.getClanService().create(c);
        core.getClanDataService().create(c);
        addMember(c, leader);
        return c;
    }

    /**
     * Disbands a clan: removes all members from memory, deletes them and the clan from the
     * database asynchronously, then unregisters the clan.
     *
     * @param clan the clan to disband
     */
    public void disband(Clan clan) {
        core.getMemberList().removeAllByClan(clan.getName());
        core.getMemberService().deleteByClan(clan.getName());
        core.getClanService().delete(clan);
        clans.remove(clan.getName().toLowerCase());
    }

    // ── Member lifecycle ──────────────────────────────────────────────────────

    /**
     * Adds an online player to the given clan in memory and persists the member row
     * asynchronously.
     *
     * @param clan   the clan to join
     * @param player the online player joining the clan
     */
    public void addMember(Clan clan, Player player) {
        Member member = new Member(player.getName(), player.getUniqueId(), false, clan.getName());
        core.getMemberList().addMember(member);
        core.getMemberService().create(member);
    }

    /**
     * Removes a member from the given clan in memory and deletes their row asynchronously.
     *
     * @param clan       the clan the member belongs to
     * @param playerUuid Mojang UUID of the member to remove
     */
    public void removeMember(Clan clan, UUID playerUuid) {
        Member member = core.getMemberList().getMemberByUuid(playerUuid);
        if (member == null) return;
        core.getMemberService().delete(member);
        core.getMemberList().removeMember(playerUuid);
    }

    /**
     * Updates the moderator flag of a clan member in memory and persists asynchronously.
     *
     * @param clan       the clan the member belongs to
     * @param playerUuid Mojang UUID of the member
     * @param isModer    {@code true} to promote, {@code false} to demote
     */
    public void setModer(Clan clan, UUID playerUuid, boolean isModer) {
        Member member = core.getMemberList().getMemberByUuid(playerUuid);
        if (member == null) return;
        member.setModer(isModer);
        core.getMemberService().updateModer(member);
    }

    // ── Broadcast ─────────────────────────────────────────────────────────────

    /**
     * Sends a formatted message to all online members of the given clan.
     *
     * @param clan    the target clan
     * @param message the message text to broadcast
     */
    public void broadcast(Clan clan, String message) {
        for (Member member : core.getMemberList().getListOfMembers(clan.getName())) {
            Player pl = Bukkit.getPlayer(member.getPlayerUuid());
            if (pl != null) {
                pl.sendMessage(core.lang("command.broadcast_format", core.lang("chat.clan"), message));
            }
        }
    }
}
