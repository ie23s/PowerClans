package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanDataRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Handles persistence for the {@code clan_data} EAV table.
 * Read operations are synchronous (called once at startup); write operations are dispatched
 * asynchronously via the Bukkit scheduler.
 */
public class ClanDataService {

    private final Core core;
    private final ClanDataRepository repository;

    public ClanDataService(Core core, ClanDataRepository repository) {
        this.core       = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Loads all {@code clan_data} rows and applies them to the matching {@link Clan} objects.
     * Rows whose UUID does not match any loaded clan are silently skipped.
     * Rows whose ident is not a known {@link ClanDataKey} are silently skipped.
     * Must be called after {@link ClanService#loadAll()}.
     *
     * @throws SQLException if a database error occurs
     */
    public void loadAll() throws SQLException {
        for (ClanDataDto dto : repository.findAll()) {
            Clan clan = core.getClanList().getClanByUuid(dto.clanUuid());
            if (clan == null) continue;
            try {
                ClanDataKey key = ClanDataKey.fromIdent(dto.ident());
                clan.set(key, key.parse(dto.value()));
            } catch (IllegalArgumentException ignored) {
                // unknown ident stored in DB — skip
            }
        }
    }

    // ── Write (async) ─────────────────────────────────────────────────────────

    /**
     * Inserts one {@code clan_data} row per {@link ClanDataKey} for the given clan asynchronously.
     * Intended for newly created clans.
     */
    public void create(Clan clan) {
        String clanUuid = clan.getUuid();
        Map<ClanDataKey, Object> snapshot = new EnumMap<>(ClanDataKey.class);
        for (ClanDataKey key : ClanDataKey.values()) {
            snapshot.put(key, clan.getRaw(key));
        }
        async(() -> repository.insertAll(clanUuid, snapshot));
    }

    /** Updates the balance of the clan asynchronously. */
    public void updateBalance(Clan clan) {
        update(clan, ClanDataKey.BALANCE);
    }

    /** Updates the PvP flag of the clan asynchronously. */
    public void updatePvp(Clan clan) {
        update(clan, ClanDataKey.PVP);
    }

    /** Updates the home location string of the clan asynchronously. */
    public void updateHome(Clan clan) {
        update(clan, ClanDataKey.HOME);
    }

    /** Updates the mob-kill counter of the clan asynchronously. */
    public void updateMobKills(Clan clan) {
        update(clan, ClanDataKey.MOB_KILLS);
    }

    /** Updates the player-kill counter of the clan asynchronously. */
    public void updatePlayerKills(Clan clan) {
        update(clan, ClanDataKey.PLAYER_KILLS);
    }

    /** Updates the cumulative online-time counter of the clan asynchronously. */
    public void updateOnlineTime(Clan clan) {
        update(clan, ClanDataKey.ONLINE_TIME);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void update(Clan clan, ClanDataKey key) {
        String clanUuid = clan.getUuid();
        String value    = String.valueOf(clan.getRaw(key));
        async(() -> repository.upsert(clanUuid, key.ident(), value));
    }

    @FunctionalInterface
    private interface SqlRunnable {
        void run() throws SQLException;
    }

    private void async(SqlRunnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
            try {
                task.run();
            } catch (SQLException e) {
                core.getUtils().getLogger().error(core.lang("other.mysql_error2"));
                core.getUtils().getLogger().error(e.getMessage());
            }
        });
    }
}
