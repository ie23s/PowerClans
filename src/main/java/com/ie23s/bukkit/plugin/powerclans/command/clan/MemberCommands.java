package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * /clan commands that manage clan membership: inviting and kicking players.
 */
public final class MemberCommands {

    private MemberCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan invite &lt;player&gt; — sends a {@link com.ie23s.bukkit.plugin.powerclans.utils.RequestType#INVITE} request to an online player.
     * Requires leader or moderator rank.
     */
    public static class Invite extends BaseClanCommand {

        /** @param core the plugin core */
        public Invite(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !hasTarget(s, args) || !inClan(s, clan)
                    || !isLeaderOrModer(s, clan, "errors._19")) return false;
            if (Bukkit.getPlayer(args[1]) == null) {
                s.sendMessage(core.lang("errors._20")); return false;
            }
            if (core.getMemberList().isMember(args[1])) {
                s.sendMessage(core.lang("errors._21")); return false;
            }
            if (clan.getMaxPlayers() <= core.getMemberList().getListOfMembers(clan.getName()).size()) {
                s.sendMessage(core.lang("errors._22", clan.getMaxPlayers())); return false;
            }
            if (Request.get(Bukkit.getPlayer(args[1])) != null) {
                s.sendMessage(core.lang("error.23")); return false;
            }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            new Request(clan, Bukkit.getPlayer(args[1]), s.getName(), RequestType.INVITE).send();
            s.sendMessage(core.lang("clan.invite", s.getName(), args[1]));
            notifyInvited(Objects.requireNonNull(Bukkit.getPlayer(args[1])), s.getName());
        }

        /**
         * Sends all invitation notification messages to the invited player.
         *
         * @param target      the player receiving the invitation
         * @param inviterName the name of the player who sent the invite
         */
        private void notifyInvited(Player target, String inviterName) {
            target.sendMessage(core.lang("clan.invite", target.getName(), inviterName));
            target.sendMessage(core.lang("clan.invite_accept"));
            target.sendMessage(core.lang("clan.invite_deny"));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan kick &lt;player&gt; — removes a member from the clan.
     * Requires leader or moderator rank; the leader cannot be kicked.
     */
    public static class Kick extends BaseClanCommand {

        /** @param core the plugin core */
        public Kick(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !hasTarget(s, args) || !inClan(s, clan)
                    || !isLeaderOrModer(s, clan, "errors._24")
                    || !isClanMember(s, clan, args[1])) return false;
            if (args[1].equalsIgnoreCase(clan.getLeader())) {
                s.sendMessage(core.lang("errors._25")); return false;
            }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            Player target = Bukkit.getPlayer(args[1]);
            clan.kick(args[1]);
            assert target != null;
            clan.broadcast(core.lang("clan.kick_1", target.getName()));
            target.sendMessage(core.lang("clan.kick_2"));
        }
    }
}
