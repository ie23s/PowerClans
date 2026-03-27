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

    /**
     * Creates a ClanDataService backed by the given repository.
     * @param core       the plugin core used for scheduling and error reporting
     * @param repository the underlying data-access object for {@code clan_data}
     */
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
     *
     * @param clan the newly created clan
     */
    public void create(Clan clan) {
        String clanUuid = clan.getUuid();
        Map<ClanDataKey, Object> snapshot = new EnumMap<>(ClanDataKey.class);
        for (ClanDataKey key : ClanDataKey.values()) {
            snapshot.put(key, clan.getRaw(key));
        }
        async(() -> repository.insertAll(clanUuid, snapshot));
    }

    /**
     * Persists the current in-memory value of {@code key} for the given clan asynchronously.
     *
     * @param clan the clan whose data changed
     * @param key  the key that was updated
     */
    public void update(Clan clan, ClanDataKey key) {
        String clanUuid = clan.getUuid();
        String value    = String.valueOf(clan.getRaw(key));
        async(() -> repository.upsert(clanUuid, key.ident(), value));
    }

    /**
     * Deletes the row for {@code key} from the given clan's data asynchronously.
     *
     * @param clan the clan whose data changed
     * @param key  the key to delete
     */
    public void delete(Clan clan, ClanDataKey key) {
        String clanUuid = clan.getUuid();
        async(() -> repository.delete(clanUuid, key.ident()));
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
                core.getUtils().getLogger().error(core.lang("other.mysql_error2"), e);
            }
        });
    }
}
