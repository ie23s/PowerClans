package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class ClanService {

    private final Core core;
    private final ClanRepository repository;

    public ClanService(Core core, ClanRepository repository) {
        this.core = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /** Loads all clan identity rows and registers them in {@link com.ie23s.bukkit.plugin.powerclans.clan.ClanList}. */
    public void loadAll() throws SQLException {
        for (ClanDto dto : repository.findAll()) {
            Clan clan = new Clan(core, dto.uuid(), dto.name(), dto.leader(), dto.maxPlayers(), dto.level());
            core.getClanList().getClans().put(dto.name().toLowerCase(), clan);
        }
    }

    // ── Write (async) ─────────────────────────────────────────────────────────

    public void create(Clan clan) {
        async(() -> repository.insert(ClanDto.from(clan)));
    }

    public void updateLeader(Clan clan) {
        async(() -> repository.updateLeader(clan.getUuid(), clan.getLeader()));
    }

    public void updateMaxPlayers(Clan clan) {
        async(() -> repository.updateMaxPlayers(clan.getUuid(), clan.getMaxPlayers()));
    }

    public void updateLevel(Clan clan) {
        async(() -> repository.updateLevel(clan.getUuid(), clan.getLevel()));
    }

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
