package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;

/**
 * Handles persistence for the {@code clan_list} table.
 * Read operations are synchronous (called once at startup); write operations are dispatched
 * asynchronously via the Bukkit scheduler.
 */
public class ClanService {

    private final Core core;
    private final ClanRepository repository;

    public ClanService(Core core, ClanRepository repository) {
        this.core = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Loads all clan identity rows from the database and registers them in
     * {@link com.ie23s.bukkit.plugin.powerclans.clan.ClanList}.
     * Must be called before {@link ClanDataService#loadAll()}.
     *
     * @throws SQLException if a database error occurs
     */
    public void loadAll() throws SQLException {
        for (ClanDto dto : repository.findAll()) {
            Clan clan = new Clan(core, dto.id(), dto.uuid(), dto.name(), dto.leader(), dto.maxPlayers(), dto.level());
            core.getClanList().getClans().put(dto.name().toLowerCase(), clan);
        }
    }

    // ── Write (async) ─────────────────────────────────────────────────────────

    /** Inserts a new clan row asynchronously. */
    public void create(Clan clan) {
        async(() -> repository.insert(ClanDto.from(clan)));
    }

    /** Updates the leader of the clan asynchronously. */
    public void updateLeader(Clan clan) {
        async(() -> repository.updateLeader(clan.getUuid(), clan.getLeader()));
    }

    /** Updates the max-players limit of the clan asynchronously. */
    public void updateMaxPlayers(Clan clan) {
        async(() -> repository.updateMaxPlayers(clan.getUuid(), clan.getMaxPlayers()));
    }

    /** Updates the level of the clan asynchronously. */
    public void updateLevel(Clan clan) {
        async(() -> repository.updateLevel(clan.getUuid(), clan.getLevel()));
    }

    /** Deletes the clan row asynchronously. */
    public void delete(Clan clan) {
        async(() -> repository.delete(clan.getUuid()));
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
