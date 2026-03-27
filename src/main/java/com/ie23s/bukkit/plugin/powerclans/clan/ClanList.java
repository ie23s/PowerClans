package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
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
 */
public class ClanList {

    private final HashMap<String, Clan> clans = new HashMap<>();
    private final Core core;

    /**
     * Creates an empty ClanList backed by the given plugin core.
     * @param core plugin core used to reach other registries
     */
    public ClanList(Core core) {
        this.core = core;
    }

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
     * Prefer this overload in event handlers where a {@link org.bukkit.entity.Player} is available.
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
     * Creates a new clan, registers it in memory, and asynchronously persists it
     * (clan row, clan_data row, and the leader's member row).
     * The clan UUID is generated in Java via {@link java.util.UUID#randomUUID()}.
     *
     * @param clan   raw clan name, may contain colour codes (stripped before storage)
     * @param leader the online founding leader player (name and UUID are taken from this object)
     * @return the newly created {@link Clan}
     */
    public Clan create(String clan, Player leader) {
        String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', clan));
        Member member = new Member(leader.getName(), leader.getUniqueId(), false, strippedName);
        Clan c = new Clan(core, 0, java.util.UUID.randomUUID().toString(), strippedName,
                leader.getUniqueId(), core.getConfig().getInt("settings.default_max"), 1);
        c.set(ClanDataKey.TAG, clan);
        clans.put(clan.toLowerCase(), c);
        core.getMemberList().addMember(member);
        core.getClanService().create(c);
        core.getClanDataService().create(c);
        core.getMemberService().create(member);
        return c;
    }

    /**
     * Returns the total number of registered clans.
     * @return the clan count
     */
    public int number() {
        return clans.size();
    }

    /**
     * Returns the raw clan map (lower-case name → {@link Clan}).
     * Intended for bulk operations such as loading from DB or iteration.
     * @return the mutable clan map
     */
    public Map<String, Clan> getClans() {
        return clans;
    }
}
