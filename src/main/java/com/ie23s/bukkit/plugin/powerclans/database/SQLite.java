package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class SQLite extends Init {
    private Connection connection = null;
    private String db;

    SQLite(String db) {
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
        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
            if (!hasConnected()) {
                connect();
            }

            try {
                Logger.debug(strip(query));
                connection.createStatement().execute(strip(query));
            } catch (Exception var2) {
                Logger.error(Language.getMessage("mysql_error2"));
                Logger.error(query);
                Logger.error(var2.getMessage());
            }

        });
    }

    void connect() {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
            connection = DriverManager.getConnection("jdbc:sqlite://" + Main.plugin.getDataFolder().getAbsolutePath() + "/" + db + ".db");
        } catch (Exception ignore) {
        }


        executeSync("CREATE TABLE IF NOT EXISTS `clan_list` (`id` INTEGER PRIMARY KEY, `name` varchar(255) NOT NULL, `tag` varchar(255) NOT NULL, `leader` varchar(255) NOT NULL, `home` varchar(255) NOT NULL, `maxplayers` int(11) NOT NULL, `pvp` tinyint(1) NOT NULL, `balance` double NOT NULL)");
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
            Logger.debug(strip(query));
            connection.createStatement().execute(strip(query));
        } catch (Exception var2) {
            Logger.error(Language.getMessage("other.mysql_error2"));
            Logger.error(query);
            Logger.error(var2.getMessage());
        }

    }

    public ResultSet executeQuery(String query) {
        if (!hasConnected()) {
            connect();
        }

        Logger.debug(strip(query));
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
                clan = new Clan(resultSet.getString("name"), resultSet.getString("tag"), resultSet.getString("leader"), resultSet.getString("home"), resultSet.getInt("maxplayers"), resultSet.getBoolean("pvp"), resultSet.getDouble("balance"));
                Clan.getClans().put(resultSet.getString("name"), clan);
            }

            resultSet = executeQuery("SELECT * FROM clan_members");

            while (resultSet.next()) {
                clan = Clan.getClan(resultSet.getString("clan"));
                if (clan == null) {
                    execute("DELETE FROM clan_members WHERE name='" + resultSet.getString("name") + "' AND clan='" + resultSet.getString("clan") + "'");
                } else {
                    Member member = new Member(resultSet.getString("name"), resultSet.getBoolean("isModer"), resultSet.getString("clan"));
                    Member.addMember(member);
                }
            }

            Logger.info(Language.getMessage("clan.loaded"));
        } catch (Exception var2) {
            Logger.error(Language.getMessage("clan.load_error"));
        }

    }

    public void createClan(Clan clan) {
        execute("INSERT INTO `clan_list`(`id`, `name`, `tag`, `leader`, `home`, `maxplayers`, `pvp`, `balance`) VALUES (null, '" + clan.getName() + "', '" + clan.getTag() + "', '" + clan.getLeader() + "', '" + clan.getHomeString() + "', " + clan.getMaxPlayers() + ", " + clan.isPvp() + ", " + clan.getBalance() + ")");
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
    public static void importUC2() {
        try {
            SQLite sqLite = new SQLite("UralClan2");
            sqLite.connect();
            ResultSet resultSet = sqLite.executeQuery("SELECT * FROM clan_list");

            Clan clan;
            while (resultSet.next()) {
                clan = Clan.create(resultSet.getString("name"), resultSet.getString("leader"));
                try {
                    clan.setHome(new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"), resultSet.getFloat("yaw"), resultSet.getFloat("pitch")));
                } catch (Exception ignore) {
                }

                try {
                    clan.setBalance(resultSet.getInt("balance"));
                } catch (Exception ignore) {
                }

                try {
                    clan.setMaxplayers(resultSet.getInt("maxplayers"));
                } catch (Exception ignore) {
                }

                try {
                    clan.setPvP(resultSet.getString("pvp").equals("1"));
                } catch (Exception ignore) {
                }
            }

            resultSet = sqLite.executeQuery("SELECT list.name AS clan_name, member.name AS member_name, member.isModer AS moder FROM clan_list AS list JOIN clan_members AS member ON member.clan=list.name");

            while (resultSet.next()) {
                Member.addMember(new Member(resultSet.getString("member_name"), resultSet.getString("moder").equals("1"), ChatColor.stripColor(resultSet.getString("clan_name"))));
            }
            sqLite.disconnect();

        } catch (Exception ignored) {
        }

    }
}
