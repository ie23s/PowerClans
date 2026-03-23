package com.ie23s.bukkit.plugin.powerclans;

import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.command.ClanCommand;
import com.ie23s.bukkit.plugin.powerclans.command.PowerClansCommand;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.configuration.YAMLHandler;
import com.ie23s.bukkit.plugin.powerclans.database.DatabaseManager;
import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanDataRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcClanRepository;
import com.ie23s.bukkit.plugin.powerclans.database.repository.impl.JdbcMemberRepository;
import com.ie23s.bukkit.plugin.powerclans.database.service.ClanDataService;
import com.ie23s.bukkit.plugin.powerclans.database.service.ClanService;
import com.ie23s.bukkit.plugin.powerclans.database.service.MemberService;
import com.ie23s.bukkit.plugin.powerclans.event.EventListener;
import com.ie23s.bukkit.plugin.powerclans.modules.level.Level;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import com.ie23s.bukkit.plugin.powerclans.utils.Utils;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class Core extends JavaPlugin {
    private FileConfiguration config;
    private Language lang;
    private DatabaseManager dbManager;
    private ClanService clanService;
    private ClanDataService clanDataService;
    private MemberService memberService;
    private ClanList clanList;
    private MemberList memberList;
    private Utils utils;
    private Level levelModule;

    public static WorldGuardPlugin getWG() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return plugin instanceof WorldGuardPlugin wg ? wg : null;
    }

    public static Economy getVault() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        assert economyProvider != null;
        return economyProvider.getProvider();
    }

    public void load() {
        YAMLHandler yamlHandler = new YAMLHandler(this);
        config = yamlHandler.createCustomConfig("config.yml");
        lang = new Language(this);
        lang.loadLang();
        utils = new Utils(this);

        dbManager = DatabaseManager.create(this);
        ConnectionProvider cp = dbManager.getConnectionProvider();
        clanService     = new ClanService(this,     new JdbcClanRepository(cp));
        clanDataService = new ClanDataService(this, new JdbcClanDataRepository(cp));
        memberService   = new MemberService(this,   new JdbcMemberRepository(cp));

        clanList   = new ClanList(this);
        memberList = new MemberList();

        try {
            clanService.loadAll();
            clanDataService.loadAll();
            memberService.loadAll();
            utils.getLogger().info(lang("clan.loaded"));
        } catch (SQLException e) {
            utils.getLogger().error(lang("clan.load_error"));
            utils.getLogger().error(e.getMessage());
        }

        levelModule = new Level(this);
        levelModule.loadModule();
    }

    @Override
    public void onDisable() {
        dbManager.disconnect();
        utils.getLogger().info(lang("other.plugin_disabled"));
    }

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        load();

        Objects.requireNonNull(this.getCommand("clan")).setExecutor(new ClanCommand(this));
        Objects.requireNonNull(this.getCommand("powerclans")).setExecutor(new PowerClansCommand(this));

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
                if (r.getType() == RequestType.INVITE) {
                    r.getPlayer().sendMessage(lang("other.invite_canceled"));
                    @SuppressWarnings("deprecation") OfflinePlayer pl = Bukkit.getOfflinePlayer(r.getSender());
                    if (pl.getPlayer() != null && pl.isOnline()) {
                        pl.getPlayer().sendMessage(lang("other.invite_canceled2", r.getPlayer().getName()));
                    }
                }
            }

        }, 0L, 20L);
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        utils.getLogger().info(lang("other.plugin_enabled", System.currentTimeMillis() - time));
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public Language getLang() {
        return lang;
    }

    public String lang(String key) {
        return lang.getMessage(key);
    }

    public String lang(String key, Object... args) {
        return lang.getMessage(key, args);
    }

    public ClanService getClanService() {
        return clanService;
    }

    public ClanDataService getClanDataService() {
        return clanDataService;
    }

    public MemberService getMemberService() {
        return memberService;
    }

    public ClanList getClanList() {
        return clanList;
    }

    public Utils getUtils() {
        return utils;
    }

    public MemberList getMemberList() {
        return this.memberList;
    }

    public Level getLevelModule() {
        return levelModule;
    }
}
