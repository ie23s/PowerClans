package com.ie23s.bukkit.plugin.powerclans.database.repository;

import com.ie23s.bukkit.plugin.powerclans.database.BaseDbTest;
import com.ie23s.bukkit.plugin.powerclans.database.dto.MemberDto;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcMemberRepositoryTest extends BaseDbTest {

    private JdbcMemberRepository repo;

    private static final MemberDto STEVE = new MemberDto("warriors", "steve", false);
    private static final MemberDto ALEX  = new MemberDto("warriors", "alex", false);

    @BeforeEach
    void setUp() {
        repo = new JdbcMemberRepository(provider);
    }

    @Test
    void findAll_returnsEmptyWhenNoMembers() throws SQLException {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void insert_thenFindAll_returnsMember() throws SQLException {
        repo.insert(STEVE);
        List<MemberDto> result = repo.findAll();

        assertEquals(1, result.size());
        MemberDto found = result.getFirst();
        assertEquals("warriors", found.clan());
        assertEquals("steve", found.name());
        assertFalse(found.isModer());
    }

    @Test
    void insert_defaultsIsModerToFalse() throws SQLException {
        repo.insert(new MemberDto("warriors", "steve", true)); // isModer ignored by INSERT
        // isModer is hardcoded to 0 in INSERT — verify it
        assertFalse(repo.findAll().getFirst().isModer());
    }

    @Test
    void updateModer_setsModerTrue() throws SQLException {
        repo.insert(STEVE);
        repo.updateModer("warriors", "steve", true);

        assertTrue(repo.findAll().getFirst().isModer());
    }

    @Test
    void updateModer_setsModerFalse() throws SQLException {
        repo.insert(STEVE);
        repo.updateModer("warriors", "steve", true);
        repo.updateModer("warriors", "steve", false);

        assertFalse(repo.findAll().getFirst().isModer());
    }

    @Test
    void updateModer_doesNotAffectOtherMembers() throws SQLException {
        repo.insert(STEVE);
        repo.insert(ALEX);
        repo.updateModer("warriors", "steve", true);

        List<MemberDto> all = repo.findAll();
        MemberDto alex = all.stream().filter(m -> m.name().equals("alex")).findFirst().orElseThrow();
        assertFalse(alex.isModer());
    }

    @Test
    void delete_removesMember() throws SQLException {
        repo.insert(STEVE);
        repo.delete("warriors", "steve");

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void delete_doesNotAffectOtherMembers() throws SQLException {
        repo.insert(STEVE);
        repo.insert(ALEX);
        repo.delete("warriors", "steve");

        List<MemberDto> remaining = repo.findAll();
        assertEquals(1, remaining.size());
        assertEquals("alex", remaining.getFirst().name());
    }

    @Test
    void deleteByClan_removesAllMembersOfClan() throws SQLException {
        repo.insert(STEVE);
        repo.insert(ALEX);
        repo.deleteByClan("warriors");

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteByClan_doesNotAffectOtherClans() throws SQLException {
        repo.insert(STEVE);
        repo.insert(new MemberDto("rangers", "notch", false));
        repo.deleteByClan("warriors");

        List<MemberDto> remaining = repo.findAll();
        assertEquals(1, remaining.size());
        assertEquals("rangers", remaining.getFirst().clan());
    }
}
