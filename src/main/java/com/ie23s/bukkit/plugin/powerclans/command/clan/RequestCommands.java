package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.IClanCommand;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

/**
 * /clan commands that handle pending confirmation requests: accepting and denying.
 */
public final class RequestCommands {

    private RequestCommands() {
    }

    // -------------------------------------------------------------------------

    /**
     * /clan accept (or /clan a) — processes the sender's pending confirmation request.
     *
     * <ul>
     *   <li>{@link RequestType#INVITE} — adds the sender to the inviting clan.</li>
     *   <li>{@link RequestType#CREATE} — re-validates, withdraws cost, creates the clan.</li>
     *   <li>{@link RequestType#DISBAND} — re-validates, then disbands the clan.</li>
     *   <li>{@link RequestType#LEAVE} — re-validates, then removes the sender from the clan.</li>
     *   <li>{@link RequestType#LEADER_TRANSFER} — re-validates, then transfers leadership.</li>
     *   <li>{@link RequestType#UPGRADE} — applies level-up abilities and broadcasts.</li>
     * </ul>
     */
    public static class Accept extends BaseClanCommand {

        private final Map<String, IClanCommand> registry;

        /**
         * @param core     the plugin core
         * @param registry the full command registry, used to re-validate CREATE/DISBAND/LEAVE/LEADER_TRANSFER requests
         */
        public Accept(Core core, Map<String, IClanCommand> registry) {
            super(core);
            this.registry = registry;
        }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            Request req = Request.get((Player) s);
            if (req == null) {
                s.sendMessage(core.lang("errors._41"));
                return;
            }
            switch (req.getType()) {
                case INVITE:
                    acceptInvite(s, req, user);
                    break;
                case CREATE:
                    acceptCreate(s, req, clan, user);
                    break;
                case DISBAND:
                    acceptDisband(s, req, clan, user);
                    break;
                case LEAVE:
                    acceptLeave(s, req, clan, user);
                    break;
                case LEADER_TRANSFER:
                    acceptLeaderTransfer(s, req, clan, user);
                    break;
                case UPGRADE:
                    acceptUpgrade(s, clan);
                    break;
            }
        }

        /**
         * Accepts a clan invitation (type 0): joins the sender to the inviting clan.
         */
        private void acceptInvite(CommandSender s, Request req, String user) {
            req.remove();
            req.getClan().broadcast(core.lang("clan.join", user));
            req.getClan().invite((Player) s);
            s.sendMessage(core.lang("clan.invitation_accept"));
        }

        /**
         * Accepts a clan creation request (type 1): charges cost and creates the clan.
         */
        //TODO Revalidate balance
        private void acceptCreate(CommandSender s, Request req, Clan clan, String user) {
            String[] a = req.getArgs();
            if (!registry.get("create").validate(s, a, clan, user)) return;
            chargeCreateCost(s);
            Clan newClan = core.getClanList().create(a[1], (Player) s);
            newClan.broadcast(core.lang("clan.created",
                    Objects.requireNonNull(core.getClanList().getClanByName(s.getName())).getName()));
        }

        /**
         * Deducts the clan creation cost from the sender's balance, if applicable.
         */
        private void chargeCreateCost(CommandSender s) {
            int cost = core.getConfig().getInt("settings.create_cost");
            if (cost == 0 || s.hasPermission("PowerClans.free.create")) return;
            try {
                Core.getVault().withdrawPlayer((OfflinePlayer) s, cost);
            } catch (Exception ignore) {
                //Ignore statement
            }
        }

        /**
         * Accepts a disband request (type 2): disbands the clan.
         */
        private void acceptDisband(CommandSender s, Request req, Clan clan, String user) {
            if (!registry.get("disband").validate(s, req.getArgs(), clan, user)) return;
            clan.broadcast(core.lang("clan.disband", clan.getName()));
            clan.disband();
        }

        /**
         * Accepts a leave request (type 3): removes the sender from the clan.
         */
        private void acceptLeave(CommandSender s, Request req, Clan clan, String user) {
            if (!registry.get("leave").validate(s, req.getArgs(), clan, user)) return;
            clan.broadcast(core.lang("clan.leave_2", s.getName()));
            clan.kick(s.getName());
        }

        /**
         * Accepts a leadership transfer request (type 4): transfers leadership to the target.
         */
        private void acceptLeaderTransfer(CommandSender s, Request req, Clan clan, String user) {
            String[] a = req.getArgs();
            if (!registry.get("leader").validate(s, a, clan, user)) return;
            if (clan.hasModer(a[1])) clan.setModer(a[1], false);
            clan.setLeader(core.getMemberList().getMember(a[1]).getPlayerUuid());
            clan.broadcast(core.lang("clan.leader", s.getName(), a[1]));
        }

        /**
         * Accepts an upgrade request (type 5): applies level-up abilities and broadcasts.
         */
        private void acceptUpgrade(CommandSender s, Clan clan) {
            clan.broadcast(core.lang("level.upgrade.reach_level", clan.getLevel()));
            core.getLevelModule().getAbilities().upgradeAbilities((Player) s, true);
            core.getLevelModule().getAbilities().makeUpgrade(clan);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan deny (or /clan d) — cancels the sender's pending confirmation request.
     */
    public static class Deny extends BaseClanCommand {

        /**
         * @param core the plugin core
         */
        public Deny(Core core) {
            super(core);
        }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            Request req = Request.get((Player) s);
            if (req == null) {
                s.sendMessage(core.lang("errors._41"));
                return;
            }
            req.remove();
            s.sendMessage(core.lang("command.deny"));
        }
    }
}
