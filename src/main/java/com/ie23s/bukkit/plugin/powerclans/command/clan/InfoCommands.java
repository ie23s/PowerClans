package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * /clan commands that display information: clan details, online members,
 * clan list, and the top-10 leaderboard.
 */
public final class InfoCommands {

    private InfoCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan info — displays name, member count, leader, level,
     * and upgrade requirements to the sender.
     */
    public static class Info extends BaseClanCommand {

        /** @param core the plugin core */
        public Info(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return perm(s, args) && inClan(s, clan);
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            s.sendMessage(core.lang("command.info_1", clan.getName(),
                    core.getMemberList().getListOfMembers(clan.getName()).size(),
                    clan.getMaxPlayers()));
            s.sendMessage(core.lang("command.info_2", clan.getLeader()));
            s.sendMessage(core.lang("command.info_3", clan.getLevel()));
            core.getLevelModule().getRequirements().upgradeRequirements((Player) s);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan online — lists all online clan members, highlighting the sender.
     */
    public static class Online extends BaseClanCommand {

        /** @param core the plugin core */
        public Online(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return perm(s, args) && inClan(s, clan);
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            s.sendMessage(core.lang("command.online_1"));
            for (String member : core.getMemberList().getListOfMembers(clan.getName())) {
                if (Bukkit.getPlayer(member) == null) continue;
                s.sendMessage(formatMemberLine(s.getName(), member));
            }
        }

        /**
         * Formats a member line, marking the sender with a "you" indicator.
         */
        private String formatMemberLine(String senderName, String member) {
            if (senderName.equalsIgnoreCase(member)) {
                return ChatColor.YELLOW + " > " + ChatColor.GREEN + member;
            }
            return ChatColor.YELLOW + " - " + member;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan list [page] — displays a paginated list of all clans (10 per page).
     */
    public static class ClanList extends BaseClanCommand {

        /** @param core the plugin core */
        public ClanList(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args)) return false;
            if (core.getClanList().number() == 0) { s.sendMessage(core.lang("errors._26")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            int offset = parsePageOffset(s, args);
            if (offset < 0) return;
            int totalPages = (int) Math.ceil((double) core.getClanList().number() / 10.0D);
            s.sendMessage(core.lang("clan.list", offset / 10 + 1, totalPages));
            int count = 0;
            for (int i = offset; i < core.getClanList().number() && count < 10; i++, count++) {
                Clan c = (Clan) core.getClanList().getClans().values().toArray()[i];
                s.sendMessage(formatClanLine(c));
            }
        }

        /**
         * Parses the optional page argument into a zero-based offset.
         * Sends an error and returns {@code -1} on invalid input or out-of-range page.
         */
        private int parsePageOffset(CommandSender s, String[] args) {
            if (args.length <= 1) return 0;
            try {
                int page = Integer.parseInt(args[1]);
                if (page < 1) throw new NumberFormatException();
                int offset = (page - 1) * 10;
                if (core.getClanList().number() - offset < 0) {
                    s.sendMessage(core.lang("error._43")); return -1;
                }
                return offset;
            } catch (Exception e) {
                s.sendMessage(core.lang("error._42")); return -1;
            }
        }

        /**
         * Formats a single clan entry line with name, member count, and leader.
         */
        private String formatClanLine(Clan c) {
            return ChatColor.YELLOW + " - " + c.getName()
                    + ChatColor.YELLOW + " ["
                    + core.getMemberList().getListOfMembers(c.getName()).size()
                    + "] (" + c.getLeader() + ")";
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan top — displays the top 10 clans sorted by member count (descending).
     */
    public static class Top extends BaseClanCommand {

        /** @param core the plugin core */
        public Top(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args)) return false;
            if (core.getClanList().number() == 0) { s.sendMessage(core.lang("errors._26")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            List<Map.Entry<Clan, Integer>> sorted = sortedByMemberCount();
            int rank = 1;
            for (Map.Entry<Clan, Integer> entry : sorted) {
                Clan c = entry.getKey();
                s.sendMessage(core.lang("command.top_1", rank, c.getName(), c.getLeader(), entry.getValue()));
                if (rank++ == 10) break;
            }
        }

        /**
         * Returns all clans sorted by member count in descending order.
         */
        private List<Map.Entry<Clan, Integer>> sortedByMemberCount() {
            Map<Clan, Integer> counts = new HashMap<>();
            for (Clan c : core.getClanList().getClans().values()) {
                counts.put(c, core.getMemberList().getListOfMembers(c.getName()).size());
            }
            LinkedList<Map.Entry<Clan, Integer>> sorted = new LinkedList<>(counts.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            return sorted;
        }
    }
}
