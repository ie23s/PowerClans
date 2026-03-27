package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * /clan commands related to in-clan chat communication.
 */
public final class ChatCommands {

    private ChatCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan msg &lt;message...&gt; — broadcasts a message to all online clan members.
     * Colour depends on the sender's rank: gold for leader, green for moderator, aqua otherwise.
     * Requires leader or moderator rank.
     */
    public static class Msg extends BaseClanCommand {

        /**
         * Creates a Msg command backed by the given plugin core.
         * @param core the plugin core
         */
        public Msg(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return perm(s, args) && inClan(s, clan) && isLeaderOrModer(s, clan, "errors._11");
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            String text = buildMessageText(args);
            if (text.length() <= 3) { s.sendMessage(core.lang("errors._40")); return; }
            clan.broadcast(core.lang("command.msg_format",
                    core.lang("command.msg_1"),
                    rankColor(s.getName(), clan) + s.getName(),
                    text));
        }

        /**
         * Joins all message words ({@code args[1..n]}) into a single space-separated string.
         */
        private String buildMessageText(String[] args) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
            return sb.toString();
        }

        /**
         * Returns the chat colour appropriate for the sender's clan rank.
         *
         * @return {@link ChatColor#GOLD} for leader, {@link ChatColor#GREEN} for moderator,
         *         {@link ChatColor#AQUA} otherwise
         */
        private ChatColor rankColor(String senderName, Clan clan) {
            if (clan.hasLeader(senderName)) return ChatColor.GOLD;
            if (clan.hasModer(senderName))  return ChatColor.GREEN;
            return ChatColor.AQUA;
        }
    }
}
