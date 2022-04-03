package com.ie23s.bukkit.plugin.powerclans.modules;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class WorldGuardUtils {
    public static boolean canBuild(Player player, Location location) {
        WorldGuardPlugin plugin = Core.getWG();
        if (plugin == null)
            return true;
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        World world = BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld()));
        if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(player), world)) {
            return query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
        }
        return false;
    }

    public static boolean getFlag(Location l, StateFlag... flags) {
        final com.sk89q.worldguard.protection.regions.RegionContainer c = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        final com.sk89q.worldguard.protection.regions.RegionQuery q = c.createQuery();
        final ApplicableRegionSet s = q.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(l));
        return s.testState(null, flags);
    }
}
