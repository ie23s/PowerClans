package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public ClanList(Core core) {
        this.core = core;
    }

    /**
     * Returns the clan with the given name, or {@code null} if not found (case-insensitive).
     *
     * @param clan clan display name
     */
    public Clan getClan(String clan) {
        return clans.get(clan.toLowerCase());
    }

    /**
     * Returns the clan with the given UUID, or {@code null} if not found.
     *
     * @param uuid clan UUID
     */
    public Clan getClanByUuid(String uuid) {
        for (Clan clan : clans.values()) {
            if (clan.getUuid().equals(uuid)) return clan;
        }
        return null;
    }

    /**
     * Returns the clan that the given player belongs to, or {@code null} if the player is not a member.
     *
     * @param player player name
     */
    public Clan getClanByName(String player) {
        if (core.getMemberList().isMember(player)) {
            return getClan(Objects.requireNonNull(core.getMemberList().getMember(player)).getClan());
        }
        return null;
    }

    /**
     * Creates a new clan, registers it in memory, and asynchronously persists it
     * (clan row, clan_data row, and the leader's member row).
     * The UUID is generated in Java via {@link java.util.UUID#randomUUID()}.
     *
     * @param clan   raw clan name, may contain colour codes (stripped before storage)
     * @param leader player name of the founding leader (will be lower-cased)
     * @return the newly created {@link Clan}
     */
    public Clan create(String clan, String leader) {
        String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', clan));
        Member member = new Member(leader.toLowerCase(), false, strippedName);
        Clan c = new Clan(core, 0, java.util.UUID.randomUUID().toString(), strippedName,
                leader.toLowerCase(), core.getConfig().getInt("settings.default_max"), 1);
        c.set(ClanDataKey.TAG, clan);
        clans.put(clan.toLowerCase(), c);
        core.getMemberList().addMember(member);
        core.getClanService().create(c);
        core.getClanDataService().create(c);
        core.getMemberService().create(member);
        return c;
    }

    /** Returns the total number of registered clans. */
    public int number() {
        return clans.size();
    }

    /**
     * Returns the raw clan map (lower-case name → {@link Clan}).
     * Intended for bulk operations such as loading from DB or iteration.
     */
    public Map<String, Clan> getClans() {
        return clans;
    }
}
