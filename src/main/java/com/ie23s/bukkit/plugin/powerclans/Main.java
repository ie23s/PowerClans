package com.ie23s.bukkit.plugin.powerclans;

import com.ie23s.bukkit.plugin.powerclans.command.ClanCommand;
import com.ie23s.bukkit.plugin.powerclans.command.PowerClansCommand;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.database.Init;
import com.ie23s.bukkit.plugin.powerclans.event.EventListener;
import com.ie23s.bukkit.plugin.powerclans.utils.Logger;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;


public class Main extends JavaPlugin {

    public static Plugin plugin;

    public static WorldGuardPlugin getWG() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return plugin instanceof WorldGuardPlugin ? (WorldGuardPlugin) plugin : null;
    }

    @SuppressWarnings({"rawtypes"})
    public static Economy getVault() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        return economyProvider.getProvider();
    }

    public static void load() {

        Configuration.loadConfig();
        Language.loadLang();

        Init.initDB();
        Init.getConnection().getClans();
    }

    public void onDisable() {
        Init.getConnection().disconnect();
        Logger.info(Language.getMessage("other.plugin_disabled"));
    }

    public void onEnable() {
        long time = System.currentTimeMillis();

        plugin = this;
        load();

        this.getCommand("clan").setExecutor(new ClanCommand());
        this.getCommand("powerclans").setExecutor(new PowerClansCommand());


        Bukkit.getScheduler().runTaskTimer(this, () -> {
            ArrayList<Request> toDelete = new ArrayList<>();
            Iterator<Request> var3 = Request.requests.iterator();

            Request r;
            while (var3.hasNext()) {
                r = var3.next();
                if (System.currentTimeMillis() - r.getTime() >= 15000L) {
                    toDelete.add(r);
                }
            }

            for (var3 = toDelete.iterator(); var3.hasNext(); r.remove()) {
                r = var3.next();
                if (r.getType() == 0) {
                    r.getPlayer().sendMessage(Language.getMessage("other.invite_canceled"));
                    @SuppressWarnings("deprecation") OfflinePlayer pl = Bukkit.getOfflinePlayer(r.getSender());
                    if (pl.isOnline()) {
                        pl.getPlayer().sendMessage(Language.getMessage("other.invite_canceled2", r.getPlayer().getName()));
                    }
                }
            }

        }, 0L, 20L);
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Logger.info(Language.getMessage("other.plugin_enabled", System.currentTimeMillis() - time));
    }
}
