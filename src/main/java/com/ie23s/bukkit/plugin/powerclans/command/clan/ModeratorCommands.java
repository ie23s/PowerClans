package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /clan commands that manage clan staff: promoting/demoting moderators
 * and transferring leadership.
 */
public final class ModeratorCommands {

    private ModeratorCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan addmoder &lt;player&gt; — promotes a clan member to moderator rank.
     * Only the clan leader may use this command.
     */
    public static class AddModer extends BaseClanCommand {

        /**
         * Creates an AddModer command backed by the given plugin core.
         * @param core the plugin core
         */
        public AddModer(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan) || !hasTarget(s, args)
                    || !isLeader(s, clan, "errors._13")
                    || !isClanMember(s, clan, args[1])) return false;
            UUID targetUuid = core.getMemberList().getMember(args[1]).getPlayerUuid();
            if (clan.hasLeader(targetUuid)) { s.sendMessage(core.lang("errors._15")); return false; }
            if (clan.hasModer(targetUuid))  { s.sendMessage(core.lang("errors._16")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            Member target = core.getMemberList().getMember(args[1]);
            core.getClanList().setModer(clan, target.getPlayerUuid(), true);
            core.getClanList().broadcast(clan, core.lang("clan.addmoder", target.getName()));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan delmoder &lt;player&gt; — removes moderator rank from a clan member.
     * Only the clan leader may use this command.
     */
    public static class DelModer extends BaseClanCommand {

        /**
         * Creates a DelModer command backed by the given plugin core.
         * @param core the plugin core
         */
        public DelModer(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan) || !hasTarget(s, args)
                    || !isLeader(s, clan, "errors._17")
                    || !isClanMember(s, clan, args[1])) return false;
            UUID targetUuid = core.getMemberList().getMember(args[1]).getPlayerUuid();
            if (!clan.hasModer(targetUuid)) { s.sendMessage(core.lang("errors._18")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            Member target = core.getMemberList().getMember(args[1]);
            core.getClanList().setModer(clan, target.getPlayerUuid(), false);
            core.getClanList().broadcast(clan, core.lang("clan.delmoder", target.getName()));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan leader &lt;player&gt; — queues a {@link RequestType#LEADER_TRANSFER} confirmation request.
     * The target must be a member of the same clan and cannot be the current leader.
     */
    public static class Leader extends BaseClanCommand {

        /**
         * Creates a Leader command backed by the given plugin core.
         * @param core the plugin core
         */
        public Leader(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan)
                    || !isLeader(s, clan, "errors._36")
                    || !hasTarget(s, args)) return false;
            if (!core.getMemberList().isMember(args[1])) {
                s.sendMessage(core.lang("errors._33")); return false;
            }
            if (!isSameClanMember(s, clan, args[1])) return false;
            if (args[1].equalsIgnoreCase(s.getName())) {
                s.sendMessage(core.lang("errors._35")); return false;
            }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            s.sendMessage(core.lang("clan.leader_request"));
            new Request(clan, (Player) s, user, RequestType.LEADER_TRANSFER, args).send();
            s.sendMessage(core.lang("command.request"));
        }

        /**
         * Checks that the target belongs to the same clan as the sender.
         * Sends an error and returns {@code false} if not.
         */
        private boolean isSameClanMember(CommandSender s, Clan clan, String target) {
            Clan targetClan = core.getClanList().getClanByName(target);
            if (targetClan == null) { s.sendMessage(core.lang("errors._14")); return false; }
            if (!targetClan.getName().equalsIgnoreCase(clan.getName())) {
                s.sendMessage(core.lang("errors._34")); return false;
            }
            return true;
        }
    }
}
