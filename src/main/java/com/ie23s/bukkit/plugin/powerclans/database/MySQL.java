package com.ie23s.bukkit.plugin.powerclans.database;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.utils.Logger;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

class MySQL extends Init {
    private Connection connection = null;

    MySQL() {
        connect();
    }

    public String strip(String str) {
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
                Logger.error(Language.getMessage("other.mysql_error2"));
                Logger.error(query);
                Logger.error(var2.getMessage());
            }

        });
    }

    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://" + Configuration.getConfiguration().getString("mysql.host") + ":" + Configuration.getConfiguration().getString("mysql.port") + "/" + Configuration.getConfiguration().getString("mysql.database") + "?useUnicode=true&characterEncoding=UTF-8&" + "user=" + Configuration.getConfiguration().getString("mysql.username") + "&password=" + Configuration.getConfiguration().getString("mysql.password"));
        } catch (Exception ignore) {
        }
        executeSync("CREATE TABLE IF NOT EXISTS `clan_list` (`id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL, `tag` varchar(255) COLLATE utf8_unicode_ci NOT NULL, `leader` varchar(255) COLLATE utf8_unicode_ci NOT NULL, `home` varchar(255) COLLATE utf8_unicode_ci NOT NULL, `maxplayers` int(11) NOT NULL, `pvp` tinyint(1) NOT NULL, `balance` double NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `name` (`name`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;)");
        executeSync("CREATE TABLE IF NOT EXISTS `clan_members` (`id` int(11) NOT NULL AUTO_INCREMENT,`clan` varchar(255) NOT NULL,`name` varchar(255) NOT NULL,`isModer` tinyint(1) NOT NULL,PRIMARY KEY (`id`)) DEFAULT CHARSET=utf8 AUTO_INCREMENT=0");

    }

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
}
