package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;

import java.sql.ResultSet;

abstract public class Init {
    private static Init connection;

    public static void initDB() {
        String DBType = Configuration.getConfiguration().getString("database").toLowerCase();
        switch (DBType) {
            case "mysql":
                connection = new MySQL();
                connection.connect();
            case "sqlite":
            default:
                connection = new SQLite("PowerClans");
                connection.connect();
        }
    }

    public static Init getConnection() {
        return connection;
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
}
