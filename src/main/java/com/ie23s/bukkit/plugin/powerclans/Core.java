package com.ie23s.bukkit.plugin.powerclans;

import com.ie23s.bukkit.plugin.powerclans.clan.ClanList;
import com.ie23s.bukkit.plugin.powerclans.clan.MemberList;
import com.ie23s.bukkit.plugin.powerclans.command.ClanCommand;
import com.ie23s.bukkit.plugin.powerclans.command.PowerClansCommand;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.configuration.YAMLHandler;
import com.ie23s.bukkit.plugin.powerclans.database.InitDB;
import com.ie23s.bukkit.plugin.powerclans.event.EventListener;
import com.ie23s.bukkit.plugin.powerclans.modules.level.Level;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class Core extends JavaPlugin {
    private FileConfiguration config;
    private Language lang;
    private InitDB db;
    private ClanList clanList;
    private MemberList memberList;
    private Utils utils;
    private Level levelModule;

    public void load() {
        YAMLHandler yamlHandler = new YAMLHandler(this);
        config = yamlHandler.createCustomConfig("config.yml");
        lang = new Language(this);
        lang.loadLang();
        utils = new Utils(this);
        db = InitDB.initDB(this);
        clanList = new ClanList(this);
        memberList = new MemberList();
        db.getClans();
        levelModule = new Level(this);
        levelModule.loadModule();
    }

    public void onDisable() {
        db.disconnect();
        utils.getLogger().info(this.getLang().getMessage("other.plugin_disabled"));
    }

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
                if (r.getType() == 0) {
                    r.getPlayer().sendMessage(this.getLang().getMessage("other.invite_canceled"));
                    @SuppressWarnings("deprecation") OfflinePlayer pl = Bukkit.getOfflinePlayer(r.getSender());
                    if (pl.isOnline()) {
                        pl.getPlayer().sendMessage(this.getLang().getMessage("other.invite_canceled2", r.getPlayer().getName()));
                    }
                }
            }

        }, 0L, 20L);
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        utils.getLogger().info(this.getLang().getMessage("other.plugin_enabled", System.currentTimeMillis() - time));
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return config;
    }

    public Language getLang() {
        return lang;
    }

    public InitDB getDb() {
        return db;
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
