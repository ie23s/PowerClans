package com.ie23s.bukkit.plugin.powerclans;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Plugins {

    public static WorldGuardPlugin getWG() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return plugin instanceof WorldGuardPlugin ? (WorldGuardPlugin) plugin : null;
    }

    public static Economy getVault() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        assert economyProvider != null;
        return economyProvider.getProvider();
    }

    public static WorldEditPlugin getWE() {
        Plugin worldEdit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        return worldEdit instanceof WorldEditPlugin ? (WorldEditPlugin) worldEdit : null;
    }
}
