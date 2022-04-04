package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class SQLite extends InitDB {
    private final Core core;
    private Connection connection = null;
    private final String db;

    SQLite(Core core, String db) {
        super.setConnection(this);
        this.core = core;
        this.db = db;
        connect();
    }

    String strip(String str) {
        str = str.replaceAll("<[^>]*>", "");
        str = str.replace("\\", "\\\\");
        str = str.trim();
        return str;
    }

    void execute(final String query) {
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
            if (!hasConnected()) {
                connect();
            }

            try {
                core.getUtils().getLogger().debug(strip(query));
                connection.createStatement().execute(strip(query));
            } catch (Exception var2) {
                core.getUtils().getLogger().error(core.getLang().getMessage("mysql_error2"));
                core.getUtils().getLogger().error(query);
                core.getUtils().getLogger().error(var2.getMessage());
            }

        });
    }

    void connect() {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
            connection = DriverManager.getConnection("jdbc:sqlite://" + core.getDataFolder().getAbsolutePath() + "/" + db + ".db");
        } catch (Exception ignore) {
        }


        executeSync("CREATE TABLE IF NOT EXISTS `clan_list` (`id` INTEGER PRIMARY KEY, `name` varchar(255) NOT NULL, `tag` varchar(255) NOT NULL, `leader` varchar(255) NOT NULL, `home` varchar(255) NOT NULL, `maxplayers` int(11) NOT NULL, `pvp` tinyint(1) NOT NULL, `balance` double NOT NULL, `mobkills` int(11) NOT NULL, `playerkills` int(11) NOT NULL, `onlinetime` int(11) NOT NULL, `level` int(11) NOT NULL)");
        executeSync("CREATE TABLE IF NOT EXISTS `clan_members` (`id` INTEGER PRIMARY KEY,`clan` varchar(255) NOT NULL,`name` varchar(255) NOT NULL,`isModer` tinyint(1) NOT NULL)");
    }

    @Override
    public boolean hasConnected() {
        try {
            return !connection.isClosed();
        } catch (Exception var1) {
            return false;
        }
    }

    public void executeSync(String query) {
        if (!hasConnected()) {
            connect();
        }

        try {
            core.getUtils().getLogger().debug(strip(query));
            connection.createStatement().execute(strip(query));
        } catch (Exception var2) {
            core.getUtils().getLogger().error(core.getLang().getMessage("other.mysql_error2"));
            core.getUtils().getLogger().error(query);
            core.getUtils().getLogger().error(var2.getMessage());
        }

    }

    public ResultSet executeQuery(String query) {
        if (!hasConnected()) {
            connect();
        }

        core.getUtils().getLogger().debug(strip(query));
        ResultSet rs = null;

        try {
            rs = connection.createStatement().executeQuery(strip(query));
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return rs;
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }

    }


    public void getClans() {
        try {
            ResultSet resultSet = executeQuery("SELECT * FROM clan_list");

            Clan clan;
            while (resultSet.next()) {
                clan = new Clan(core,
                        resultSet.getString("name"),
                        resultSet.getString("tag"),
                        resultSet.getString("leader"),
                        resultSet.getString("home"),
                        resultSet.getInt("maxplayers"),
                        resultSet.getBoolean("pvp"),
                        resultSet.getDouble("balance"),
                        resultSet.getInt("mobkills"),
                        resultSet.getInt("playerkills"),
                        resultSet.getInt("onlinetime"),
                        resultSet.getInt("level"));
                core.getClanList().getClans().put(resultSet.getString("name"), clan);
            }

            resultSet = executeQuery("SELECT * FROM clan_members");

            while (resultSet.next()) {
                clan = core.getClanList().getClan(resultSet.getString("clan"));
                if (clan == null) {
                    execute("DELETE FROM clan_members WHERE name='" + resultSet.getString("name") + "' AND clan='" + resultSet.getString("clan") + "'");
                } else {
                    Member member = new Member(resultSet.getString("name"), resultSet.getBoolean("isModer"), resultSet.getString("clan"));
                    core.getMemberList().addMember(member);
                }
            }

            core.getUtils().getLogger().info(core.getLang().getMessage("clan.loaded"));
        } catch (Exception var2) {
            core.getUtils().getLogger().error(core.getLang().getMessage("clan.load_error"));
        }

    }

    public void createClan(Clan clan) {
        execute("INSERT INTO `clan_list`(`id`, `name`, `tag`, `leader`, `home`, `maxplayers`, `pvp`, `balance`, `mobkills`, `playerkills`, `onlinetime`, `level`) VALUES (null, '" + clan.getName() + "', '" + clan.getTag() + "', '" + clan.getLeader() + "', '" + clan.getHomeString() + "', " + clan.getMaxPlayers() + ", " + clan.isPvp() + ", " + clan.getBalance() + ", 0, 0, 0, 1)");
    }

    public void createClanMember(Member member) {
        this.execute("INSERT INTO clan_members (clan, name, isModer) VALUES ('" + member.getClan() + "', '" + member.getName() + "', '0')");
    }

    public void setLeader(Member member) {
        this.execute("UPDATE clan_list SET leader='" + member.getName() + "' WHERE name='" + member.getClan() + "'");
    }

    public void setBalance(Clan clan) {
        this.execute("UPDATE clan_list SET balance='" + clan.getBalance() + "' WHERE name='" + clan.getName() + "'");
    }

    public void setPvP(Clan clan) {
        this.execute("UPDATE clan_list SET pvp='" + (clan.isPvP() ? "1" : "0") + "' WHERE name='" + clan.getName() + "'");
    }

    public void kick(Member member) {
        this.execute("DELETE FROM clan_members WHERE clan='" + member.getName() + "' AND name='" + member.getClan() + "'");
    }

    public void setModer(Member member) {
        this.execute("UPDATE clan_members SET isModer='" + (member.isModer() ? "1" : "0") + "' WHERE clan='" + member.getClan() + "' AND name='" + member.getName() + "'");
    }

    public void disband(String name) {
        this.execute("DELETE FROM clan_list WHERE name='" + name + "'");
        this.execute("DELETE FROM clan_members WHERE clan='" + name + "'");
    }

    public void setHome(Clan clan) {
        this.execute("UPDATE clan_list SET home='" + clan.getHomeString() + "' WHERE name='" + clan.getName() + "'");
    }

    public void clanUpgrade(Clan clan) {
        this.execute("UPDATE clan_list SET maxplayers='" + clan.getMaxPlayers() + "' WHERE name='" + clan.getName() + "'");

    }

    @Override
    public void setMobKills(Clan clan) {
        this.execute("UPDATE clan_list SET `mobkills`='" + clan.getMobKills() + "' WHERE name='" + clan.getName() + "'");
    }

    @Override
    public void setPlayerKills(Clan clan) {
        this.execute("UPDATE clan_list SET `playerkills`='" + clan.getPlayerKills() + "' WHERE name='" + clan.getName() + "'");
    }

    @Override
    public void setOnlineTime(Clan clan) {
        System.out.println("UPDATE clan_list SET `onlinetime`='" + clan.getOnlineTime() + "' WHERE name='" + clan.getName() + "'");
        this.execute("UPDATE clan_list SET `onlinetime`='" + clan.getOnlineTime() + "' WHERE name='" + clan.getName() + "'");


    }

    @Override
    public void setLevel(Clan clan) {
        this.execute("UPDATE clan_list SET `level`='" + clan.getLevel() + "' WHERE name='" + clan.getName() + "'");
    }
}
