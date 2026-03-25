package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.MemberRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;

/**
 * Handles persistence for the {@code clan_members} table.
 * Read operations are synchronous (called once at startup); write operations are dispatched
 * asynchronously via the Bukkit scheduler.
 */
public class MemberService {

    private final Core core;
    private final MemberRepository repository;

    public MemberService(Core core, MemberRepository repository) {
        this.core = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Loads all member rows into {@link com.ie23s.bukkit.plugin.powerclans.clan.MemberList}.
     * Rows whose clan uuid no longer exists are treated as orphans and deleted asynchronously.
     *
     * @throws SQLException if a database error occurs
     */
    public void loadAll() throws SQLException {
        for (MemberDto dto : repository.findAll()) {
            Clan clan = core.getClanList().getClanByUuid(dto.clanUuid());
            if (clan == null) {
                String uuid = dto.clanUuid();
                async(() -> repository.deleteByClan(uuid));
            } else {
                core.getMemberList().addMember(new Member(dto.name(), dto.isModer(), clan.getName()));
            }
        }
    }

    // ── Write (async) ─────────────────────────────────────────────────────────

    /** Inserts a new member row asynchronously. */
    public void create(Member member) {
        String clanUuid = clanUuid(member.getClan());
        async(() -> repository.insert(new MemberDto(clanUuid, member.getName(), member.isModer())));
    }

    /** Updates the moderator status of the member asynchronously. */
    public void updateModer(Member member) {
        String clanUuid = clanUuid(member.getClan());
        async(() -> repository.updateModer(clanUuid, member.getName(), member.isModer()));
    }

    /** Removes the member from their clan asynchronously. */
    public void delete(Member member) {
        String clanUuid = clanUuid(member.getClan());
        async(() -> repository.delete(clanUuid, member.getName()));
    }

    /** Removes all members of the given clan asynchronously. */
    public void deleteByClan(String clanName) {
        String clanUuid = clanUuid(clanName);
        async(() -> repository.deleteByClan(clanUuid));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private String clanUuid(String clanName) {
        Clan clan = core.getClanList().getClan(clanName);
        return clan != null ? clan.getUuid() : "";
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
                core.getUtils().getLogger().error(core.lang("other.mysql_error2"), e);
            }
        });
    }
}
