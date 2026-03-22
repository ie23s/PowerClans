package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;

import java.sql.ResultSet;
import java.util.Objects;

public abstract class InitDB {
    private InitDB connection = null;


    public static InitDB initDB(Core core) {
        InitDB connection;
        String dbtype = Objects.requireNonNull(core.getConfig().getString("database")).toLowerCase();
        switch (dbtype) {
            case "mysql":
                connection = new MySQL(core);
                connection.connect();
                break;
            case "sqlite":
            default:
                connection = new SQLite(core, "PowerClans");
                connection.connect();
        }
        return connection;
    }

    @SuppressWarnings("unused")
    public InitDB getConnection() {
        return connection;
    }

    public void setConnection(InitDB connection) {
        this.connection = connection;
    }

    abstract String strip(String str);

    abstract void execute(final String query);

    abstract void connect();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean hasConnected();

    public abstract void executeSync(String query);

    public abstract ResultSet executeQuery(String query);

    public abstract void disconnect();

    public abstract void getClans();

    public abstract void createClan(Clan clan);

    public abstract void createClanMember(Member member);

    public abstract void setLeader(Member member);

    public abstract void setBalance(Clan clan);

    public abstract void setPvP(Clan clan);

    public abstract void kick(Member member);

    public abstract void setModer(Member member);

    public abstract void disband(String name);

    public abstract void setHome(Clan clan);

    public abstract void clanUpgrade(Clan clan);


    public abstract void setMobKills(Clan clan);

    public abstract void setPlayerKills(Clan clan);

    public abstract void setOnlineTime(Clan clan);

    public abstract void setLevel(Clan clan);
}
