package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanDataRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;

/**
 * Handles persistence for the {@code clan_data} table.
 * Read operations are synchronous (called once at startup); write operations are dispatched
 * asynchronously via the Bukkit scheduler.
 */
public class ClanDataService {

    private final Core core;
    private final ClanDataRepository repository;

    public ClanDataService(Core core, ClanDataRepository repository) {
        this.core = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Loads all {@code clan_data} rows and fills the {@link com.ie23s.bukkit.plugin.powerclans.api.IClanData}
     * map of the matching {@link Clan}. Rows whose UUID does not match any loaded clan are silently skipped.
     * Must be called after {@link ClanService#loadAll()}.
     *
     * @throws SQLException if a database error occurs
     */
    public void loadAll() throws SQLException {
        for (ClanDataDto dto : repository.findAll()) {
            Clan clan = core.getClanList().getClanByUuid(dto.clanUuid());
            if (clan == null) continue;
            clan.set(ClanDataKey.TAG,          dto.tag());
            clan.set(ClanDataKey.HOME,         dto.home());
            clan.set(ClanDataKey.PVP,          dto.pvp());
            clan.set(ClanDataKey.BALANCE,      dto.balance());
            clan.set(ClanDataKey.MOB_KILLS,    dto.mobKills());
            clan.set(ClanDataKey.PLAYER_KILLS, dto.playerKills());
            clan.set(ClanDataKey.ONLINE_TIME,  dto.onlineTime());
        }
    }

    // ── Write (async) ─────────────────────────────────────────────────────────

    /** Inserts a new clan_data row asynchronously. */
    public void create(Clan clan) {
        async(() -> repository.insert(ClanDataDto.from(clan, clan)));
    }

    /** Updates the balance of the clan asynchronously. */
    public void updateBalance(Clan clan) {
        async(() -> repository.updateBalance(clan.getUuid(), clan.getDouble(ClanDataKey.BALANCE)));
    }

    /** Updates the PvP flag of the clan asynchronously. */
    public void updatePvp(Clan clan) {
        async(() -> repository.updatePvp(clan.getUuid(), clan.getBoolean(ClanDataKey.PVP)));
    }

    /** Updates the home location string of the clan asynchronously. */
    public void updateHome(Clan clan) {
        async(() -> repository.updateHome(clan.getUuid(), clan.getHomeString()));
    }

    /** Updates the mob-kill counter of the clan asynchronously. */
    public void updateMobKills(Clan clan) {
        async(() -> repository.updateMobKills(clan.getUuid(), clan.getInt(ClanDataKey.MOB_KILLS)));
    }

    /** Updates the player-kill counter of the clan asynchronously. */
    public void updatePlayerKills(Clan clan) {
        async(() -> repository.updatePlayerKills(clan.getUuid(), clan.getInt(ClanDataKey.PLAYER_KILLS)));
    }

    /** Updates the cumulative online-time counter of the clan asynchronously. */
    public void updateOnlineTime(Clan clan) {
        async(() -> repository.updateOnlineTime(clan.getUuid(), clan.getInt(ClanDataKey.ONLINE_TIME)));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

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
