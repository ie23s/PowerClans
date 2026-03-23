package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.database.dto.ClanDataDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.ClanDataRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class ClanDataService {

    private final Core core;
    private final ClanDataRepository repository;

    public ClanDataService(Core core, ClanDataRepository repository) {
        this.core = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /** Loads all clan_data rows and fills the data map of the matching {@link Clan}. */
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

    public void create(Clan clan) {
        async(() -> repository.insert(ClanDataDto.from(clan, clan)));
    }

    public void updateBalance(Clan clan) {
        async(() -> repository.updateBalance(clan.getUuid(), clan.getDouble(ClanDataKey.BALANCE)));
    }

    public void updatePvp(Clan clan) {
        async(() -> repository.updatePvp(clan.getUuid(), clan.getBoolean(ClanDataKey.PVP)));
    }

    public void updateHome(Clan clan) {
        async(() -> repository.updateHome(clan.getUuid(), clan.getHomeString()));
    }

    public void updateMobKills(Clan clan) {
        async(() -> repository.updateMobKills(clan.getUuid(), clan.getInt(ClanDataKey.MOB_KILLS)));
    }

    public void updatePlayerKills(Clan clan) {
        async(() -> repository.updatePlayerKills(clan.getUuid(), clan.getInt(ClanDataKey.PLAYER_KILLS)));
    }

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
