package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.LocationSerializer;
import com.ie23s.bukkit.plugin.powerclans.utils.WorldGuardUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /clan commands that manage the clan home: teleporting, setting, and removing.
 */
public final class HomeCommands {

    private HomeCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan home — teleports the sender to the clan home (with warmup).
     */
    public static class Home extends BaseClanCommand {

        /** @param core the plugin core */
        public Home(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan)) return false;
            if (clan.getString(ClanDataKey.HOME) == null) { s.sendMessage(core.lang("errors._28")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            core.getUtils().getWarm().addPlayer((Player) s, clan);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan sethome — sets the clan home to the sender's current location.
     * Requires leader rank, WorldGuard build permission, and the minimum member threshold.
     */
    public static class SetHome extends BaseClanCommand {

        /** @param core the plugin core */
        public SetHome(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan) || !isLeader(s, clan, "errors._30")) return false;
            if (!WorldGuardUtils.canBuild((Player) s, ((Player) s).getLocation())) {
                s.sendMessage(core.lang("errors._31")); return false;
            }
            return meetsMinMemberCount(s, clan);
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            clan.update(ClanDataKey.HOME, LocationSerializer.serialize(((Player) s).getLocation()));
            clan.broadcast(core.lang("clan.sethome", s.getName()));
        }

        /**
         * Checks that the clan has at least the configured minimum number of members.
         * Sends {@code errors._32} and returns {@code false} if not.
         */
        private boolean meetsMinMemberCount(CommandSender s, Clan clan) {
            int min = core.getConfig().getInt("settings.home_min");
            if (core.getMemberList().getListOfMembers(clan.getName()).size() < min) {
                s.sendMessage(core.lang("errors._32", min)); return false;
            }
            return true;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan removehome — removes the clan's home point.
     * Requires leader rank and an existing home.
     */
    public static class RemoveHome extends BaseClanCommand {

        /** @param core the plugin core */
        public RemoveHome(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan) || !isLeader(s, clan, "errors._29")) return false;
            if (clan.getString(ClanDataKey.HOME) == null) { s.sendMessage(core.lang("errors._28")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            clan.delete(ClanDataKey.HOME);
            clan.broadcast(core.lang("clan.removehome", s.getName()));
        }
    }
}
