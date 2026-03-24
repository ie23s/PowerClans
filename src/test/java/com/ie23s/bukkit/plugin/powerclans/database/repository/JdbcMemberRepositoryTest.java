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

    private static final String WARRIORS_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RANGERS_UUID  = "550e8400-e29b-41d4-a716-446655440001";

    private static final MemberDto STEVE = new MemberDto(WARRIORS_UUID, "steve", false);
    private static final MemberDto ALEX  = new MemberDto(WARRIORS_UUID, "alex",  false);

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
        assertEquals(WARRIORS_UUID, found.clanUuid());
        assertEquals("steve", found.name());
        assertFalse(found.isModer());
    }

    @Test
    void insert_defaultsIsModerToFalse() throws SQLException {
        repo.insert(new MemberDto(WARRIORS_UUID, "steve", true)); // isModer ignored by INSERT
        // isModer is hardcoded to 0 in INSERT — verify it
        assertFalse(repo.findAll().getFirst().isModer());
    }

    @Test
    void updateModer_setsModerTrue() throws SQLException {
        repo.insert(STEVE);
        repo.updateModer(WARRIORS_UUID, "steve", true);

        assertTrue(repo.findAll().getFirst().isModer());
    }

    @Test
    void updateModer_setsModerFalse() throws SQLException {
        repo.insert(STEVE);
        repo.updateModer(WARRIORS_UUID, "steve", true);
        repo.updateModer(WARRIORS_UUID, "steve", false);

        assertFalse(repo.findAll().getFirst().isModer());
    }

    @Test
    void updateModer_doesNotAffectOtherMembers() throws SQLException {
        repo.insert(STEVE);
        repo.insert(ALEX);
        repo.updateModer(WARRIORS_UUID, "steve", true);

        List<MemberDto> all = repo.findAll();
        MemberDto alex = all.stream().filter(m -> m.name().equals("alex")).findFirst().orElseThrow();
        assertFalse(alex.isModer());
    }

    @Test
    void delete_removesMember() throws SQLException {
        repo.insert(STEVE);
        repo.delete(WARRIORS_UUID, "steve");

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void delete_doesNotAffectOtherMembers() throws SQLException {
        repo.insert(STEVE);
        repo.insert(ALEX);
        repo.delete(WARRIORS_UUID, "steve");

        List<MemberDto> remaining = repo.findAll();
        assertEquals(1, remaining.size());
        assertEquals("alex", remaining.getFirst().name());
    }

    @Test
    void deleteByClan_removesAllMembersOfClan() throws SQLException {
        repo.insert(STEVE);
        repo.insert(ALEX);
        repo.deleteByClan(WARRIORS_UUID);

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteByClan_doesNotAffectOtherClans() throws SQLException {
        repo.insert(STEVE);
        repo.insert(new MemberDto(RANGERS_UUID, "notch", false));
        repo.deleteByClan(WARRIORS_UUID);

        List<MemberDto> remaining = repo.findAll();
        assertEquals(1, remaining.size());
        assertEquals(RANGERS_UUID, remaining.getFirst().clanUuid());
    }
}
