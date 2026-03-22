package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * /clan commands that manage clan lifecycle: creation, disbanding, and leaving.
 */
public final class LifecycleCommands {

    private LifecycleCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan create &lt;name&gt; — validates the name, checks the creation cost,
     * and queues a {@link com.ie23s.bukkit.plugin.powerclans.utils.RequestType#CREATE} confirmation request.
     */
    public static class Create extends BaseClanCommand {

        /** @param core the plugin core */
        public Create(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args)) return false;
            if (args.length == 1) { s.sendMessage(core.lang("errors._2")); return false; }
            if (clan != null)     { s.sendMessage(core.lang("errors._3")); return false; }
            return validateClanName(s, args[1]) && hasSufficientFunds(s);
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            new Request((Player) s, user, RequestType.CREATE, args).send();
            s.sendMessage(core.lang("clan.create_request", args[1]));
            if (!hasSufficientFunds(s)) {
                s.sendMessage(core.lang("create_request_cost", args[1]));
                return;
            }
            s.sendMessage(core.lang("command.request"));
        }

        /**
         * Validates all naming rules: uniqueness, length, and regex.
         *
         * @param s   the command sender to notify on failure
         * @param raw the raw clan name argument (may contain color codes)
         * @return {@code true} if the name passes all checks
         */
        private boolean validateClanName(CommandSender s, String raw) {
            String name = ChatColor.stripColor(raw.replaceAll("&", "§"));
            if (core.getClanList().getClan(name) != null) {
                s.sendMessage(core.lang("errors._4")); return false;
            }
            if (name.length() > core.getConfig().getInt("settings.max_symbols")) {
                s.sendMessage(core.lang("errors._5", core.getConfig().getInt("settings.max_symbols"))); return false;
            }
            if (name.length() < core.getConfig().getInt("settings.min_symbols")) {
                s.sendMessage(core.lang("errors._6", core.getConfig().getInt("settings.min_symbols"))); return false;
            }
            if (!name.matches(Objects.requireNonNull(core.getConfig().getString("settings.clan_regex")))) {
                s.sendMessage(core.lang("errors._7")); return false;
            }
            return true;
        }

        /**
         * Checks the sender can afford the creation cost (if any).
         * Silently passes when Vault is unavailable or the cost is zero.
         *
         * @param s the command sender
         * @return {@code true} if the sender can pay
         */
        private boolean hasSufficientFunds(CommandSender s) {
            int cost = core.getConfig().getInt("settings.create_cost");
            if (cost == 0 || s.hasPermission("PowerClans.free.create")) return true;
            try {
                if (!Core.getVault().has((OfflinePlayer) s, cost)) {
                    s.sendMessage(core.lang("errors._8")); return false;
                }
            } catch (Exception ignored) {}
            return true;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan disband — queues a {@link com.ie23s.bukkit.plugin.powerclans.utils.RequestType#DISBAND} confirmation request.
     * Only the clan leader may use this command.
     */
    public static class Disband extends BaseClanCommand {

        /** @param core the plugin core */
        public Disband(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return perm(s, args) && inClan(s, clan) && isLeader(s, clan, "errors._10");
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            s.sendMessage(core.lang("clan.disband_request"));
            new Request(clan, (Player) s, user, RequestType.DISBAND, args).send();
            s.sendMessage(core.lang("command.request"));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan leave — queues a {@link com.ie23s.bukkit.plugin.powerclans.utils.RequestType#LEAVE} confirmation request.
     * The clan leader must use /clan disband instead.
     */
    public static class Leave extends BaseClanCommand {

        /** @param core the plugin core */
        public Leave(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan)) return false;
            if (clan.hasLeader(s.getName())) { s.sendMessage(core.lang("errors._27")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            s.sendMessage(core.lang("clan.leave_request"));
            new Request(clan, (Player) s, user, RequestType.LEAVE, args).send();
            s.sendMessage(core.lang("command.request"));
        }
    }
}
