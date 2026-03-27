package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a paginated help page for the /clan command.
 * The list of visible commands is filtered by the sender's permissions and clan role.
 * Five commands are shown per page.
 */
public class ReferenceCommand extends BaseClanCommand {

    /**
     * Creates a ReferenceCommand backed by the given plugin core.
     *
     * @param core the plugin core
     */
    public ReferenceCommand(Core core) { super(core); }

    @Override
    public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
        return true;
    }

    @Override
    public void execute(CommandSender s, String[] args, Clan clan, String user) {
        show(s, args, clan);
    }

    /**
     * Sends the paginated help page to the given sender.
     *
     * @param s    the command sender
     * @param args full command arguments; {@code args[0]} is optionally the page number
     * @param clan the sender's current clan, or {@code null} if none
     */
    public void show(CommandSender s, String[] args, Clan clan) {
        List<String> cmds = buildCommandList(s, clan);
        int page = parsePage(args);
        s.sendMessage(core.lang("reference._1"));
        for (int i = (page - 1) * 5; i < page * 5 && i < cmds.size(); i++) {
            s.sendMessage(core.lang("reference.start") + core.lang("reference." + cmds.get(i)));
        }
        if (page * 5 <= cmds.size()) {
            s.sendMessage(core.lang("reference._2", page + 1));
        }
    }

    /**
     * Builds the ordered list of command names the sender may see,
     * based on their permissions and clan role.
     */
    private List<String> buildCommandList(CommandSender s, Clan clan) {
        List<String> cmds = new ArrayList<>();
        if (!(s instanceof Player)) return cmds;
        if (clan == null) {
            addIfPerm(s, cmds, "create");
        } else {
            java.util.UUID uuid = ((Player) s).getUniqueId();
            if (clan.hasLeader(uuid)) {
                addIfPerm(s, cmds, "disband", "leader", "addmoder", "delmoder",
                        "take", "sethome", "removehome", "upgrade");
            }
            if (clan.hasModer(uuid) || clan.hasLeader(uuid)) {
                addIfPerm(s, cmds, "msg", "invite", "kick", "pvp");
            }
            addIfPerm(s, cmds, "info", "online", "home", "balance", "deposit");
        }
        addIfPerm(s, cmds, "list", "top");
        return cmds;
    }

    /**
     * Parses the page number from {@code args[0]}, defaulting to 1 if absent or invalid.
     */
    private int parsePage(String[] args) {
        if (args.length == 0) return 1;
        try { return Integer.parseInt(args[0]); } catch (Exception ignore) { return 1; }
    }

    private void addIfPerm(CommandSender s, List<String> cmds, String... names) {
        for (String name : names) {
            if (s.hasPermission("PowerClans." + name)) cmds.add(name);
        }
    }
}
