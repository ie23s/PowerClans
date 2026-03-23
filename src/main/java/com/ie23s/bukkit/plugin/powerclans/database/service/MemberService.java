package com.ie23s.bukkit.plugin.powerclans.database.service;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.MemberRepository;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class MemberService {

    private final Core core;
    private final MemberRepository repository;

    public MemberService(Core core, MemberRepository repository) {
        this.core = core;
        this.repository = repository;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /** Loads all member rows into {@link com.ie23s.bukkit.plugin.powerclans.clan.MemberList}.
     *  Orphaned rows (clan no longer exists) are deleted asynchronously. */
    public void loadAll() throws SQLException {
        for (MemberDto dto : repository.findAll()) {
            if (core.getClanList().getClan(dto.clan()) == null) {
                async(() -> repository.delete(dto.clan(), dto.name()));
            } else {
                core.getMemberList().addMember(new Member(dto.name(), dto.isModer(), dto.clan()));
            }
        }
    }

    // ── Write (async) ─────────────────────────────────────────────────────────

    public void create(Member member) {
        async(() -> repository.insert(MemberDto.from(member)));
    }

    public void updateModer(Member member) {
        async(() -> repository.updateModer(member.getClan(), member.getName(), member.isModer()));
    }

    public void delete(Member member) {
        async(() -> repository.delete(member.getClan(), member.getName()));
    }

    public void deleteByClan(String clanName) {
        async(() -> repository.deleteByClan(clanName));
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
