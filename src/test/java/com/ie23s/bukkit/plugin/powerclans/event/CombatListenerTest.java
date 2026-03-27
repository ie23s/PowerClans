package com.ie23s.bukkit.plugin.powerclans.event;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.utils.WorldGuardUtils;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class CombatListenerTest {

    static final UUID DAMAGER_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID VICTIM_UUID  = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    static final UUID ALICE_UUID   = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Mock Core core;
    @Mock FileConfiguration config;
    @Mock ClanList clanList;
    @Mock MemberList memberList;
    @Mock Clan clan;
    @Mock Player victim;
    @Mock Player damager;
    @Mock Location victimLocation;

    CombatListener listener;

    @BeforeEach
    void setUp() {
        when(core.getConfig()).thenReturn(config);
        when(core.getClanList()).thenReturn(clanList);
        when(core.getMemberList()).thenReturn(memberList);
        when(config.getBoolean("settings.pvp")).thenReturn(false);
        when(victim.getName()).thenReturn("victim");
        when(damager.getName()).thenReturn("damager");
        when(victim.getUniqueId()).thenReturn(VICTIM_UUID);
        when(damager.getUniqueId()).thenReturn(DAMAGER_UUID);
        when(victim.getLocation()).thenReturn(victimLocation);
        listener = new CombatListener(core);
    }

    private EntityDamageByEntityEvent damageEvent(Player victim, Player damager) {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamager()).thenReturn(damager);
        return event;
    }

    // ── Friendly fire cancelled ───────────────────────────────────────────────

    @Test
    void friendlyFire_isCancelled_whenSameClanAndPvpProtectionOn() {
        when(memberList.isMemberByUuid(DAMAGER_UUID)).thenReturn(true);
        when(memberList.isMemberByUuid(VICTIM_UUID)).thenReturn(true);
        when(clanList.getClanByPlayerUuid(DAMAGER_UUID)).thenReturn(clan);
        when(clan.hasClanMember(VICTIM_UUID)).thenReturn(true);
        when(clan.getBoolean(ClanDataKey.PVP)).thenReturn(true);
        when(core.lang(anyString())).thenReturn("");

        EntityDamageByEntityEvent event = damageEvent(victim, damager);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(eq(victimLocation), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event).setCancelled(true);
    }

    // ── Friendly fire NOT cancelled ───────────────────────────────────────────

    @Test
    void friendlyFire_notCancelled_whenDamagerNotInClan() {
        when(memberList.isMemberByUuid(DAMAGER_UUID)).thenReturn(false);

        EntityDamageByEntityEvent event = damageEvent(victim, damager);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }

    @Test
    void friendlyFire_notCancelled_whenVictimNotInClan() {
        when(memberList.isMemberByUuid(DAMAGER_UUID)).thenReturn(true);
        when(memberList.isMemberByUuid(VICTIM_UUID)).thenReturn(false);

        EntityDamageByEntityEvent event = damageEvent(victim, damager);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }

    @Test
    void friendlyFire_notCancelled_whenDifferentClans() {
        when(memberList.isMemberByUuid(DAMAGER_UUID)).thenReturn(true);
        when(memberList.isMemberByUuid(VICTIM_UUID)).thenReturn(true);
        when(clanList.getClanByPlayerUuid(DAMAGER_UUID)).thenReturn(clan);
        when(clan.hasClanMember(VICTIM_UUID)).thenReturn(false);

        EntityDamageByEntityEvent event = damageEvent(victim, damager);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }

    @Test
    void friendlyFire_notCancelled_whenClanPvpProtectionOff() {
        when(memberList.isMemberByUuid(DAMAGER_UUID)).thenReturn(true);
        when(memberList.isMemberByUuid(VICTIM_UUID)).thenReturn(true);
        when(clanList.getClanByPlayerUuid(DAMAGER_UUID)).thenReturn(clan);
        when(clan.hasClanMember(VICTIM_UUID)).thenReturn(true);
        when(clan.getBoolean(ClanDataKey.PVP)).thenReturn(false);

        EntityDamageByEntityEvent event = damageEvent(victim, damager);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }

    @Test
    void friendlyFire_notCancelled_whenWorldGuardAllowsPvp() {
        EntityDamageByEntityEvent event = damageEvent(victim, damager);
        when(config.getBoolean("settings.pvp")).thenReturn(true);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(eq(victimLocation), eq(Flags.PVP))).thenReturn(true);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }

    @Test
    void friendlyFire_notCancelled_whenSelfDamage() {
        when(victim.getUniqueId()).thenReturn(ALICE_UUID);
        when(damager.getUniqueId()).thenReturn(ALICE_UUID);
        when(memberList.isMemberByUuid(ALICE_UUID)).thenReturn(true);
        when(clanList.getClanByPlayerUuid(ALICE_UUID)).thenReturn(clan);
        when(clan.hasClanMember(ALICE_UUID)).thenReturn(true);
        when(clan.getBoolean(ClanDataKey.PVP)).thenReturn(true);

        EntityDamageByEntityEvent event = damageEvent(victim, damager);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }

    // ── Projectile unwrapping ─────────────────────────────────────────────────

    @Test
    void arrowDamage_resolvesToShooter() {
        when(memberList.isMemberByUuid(DAMAGER_UUID)).thenReturn(true);
        when(memberList.isMemberByUuid(VICTIM_UUID)).thenReturn(true);
        when(clanList.getClanByPlayerUuid(DAMAGER_UUID)).thenReturn(clan);
        when(clan.hasClanMember(VICTIM_UUID)).thenReturn(true);
        when(clan.getBoolean(ClanDataKey.PVP)).thenReturn(true);
        when(core.lang(anyString())).thenReturn("");

        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(damager);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamager()).thenReturn(arrow);

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event).setCancelled(true);
    }

    @Test
    void nonPlayerDamager_isIgnored() {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamager()).thenReturn(mock(org.bukkit.entity.Skeleton.class));

        try (MockedStatic<WorldGuardUtils> wg = mockStatic(WorldGuardUtils.class)) {
            wg.when(() -> WorldGuardUtils.getFlag(any(), any())).thenReturn(false);
            listener.onEntityDamageByEntity(event);
        }

        verify(event, never()).setCancelled(true);
    }
}
