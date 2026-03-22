package com.ie23s.bukkit.plugin.powerclans.command;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.IClanCommand;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.command.clan.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Main dispatcher for the /clan command.
 * Resolves each subcommand by name from a registry and delegates to its
 * {@link IClanCommand} handler. Stateless — all per-invocation state is passed
 * as method parameters.
 */
public class ClanCommand implements CommandExecutor {

    private final Core core;
    private final Map<String, IClanCommand> commands = new HashMap<>();
    private final ReferenceCommand reference;

    /**
     * Constructs the dispatcher and registers all command handlers.
     *
     * @param core the plugin core instance
     */
    public ClanCommand(Core core) {
        this.core = core;

        // Lifecycle
        register("create",  new LifecycleCommands.Create(core));
        register("disband", new LifecycleCommands.Disband(core));
        register("leave",   new LifecycleCommands.Leave(core));

        // Members
        register("invite", new MemberCommands.Invite(core));
        register("kick",   new MemberCommands.Kick(core));

        // Moderators & leadership
        register("addmoder",   new ModeratorCommands.AddModer(core));
        register("delmoder",   new ModeratorCommands.DelModer(core));
        register("leader",     new ModeratorCommands.Leader(core));

        // Information
        register("info",    new InfoCommands.Info(core));
        register("online",  new InfoCommands.Online(core));
        register("list",    new InfoCommands.ClanList(core));
        register("top",     new InfoCommands.Top(core));

        // Home
        register("home",       new HomeCommands.Home(core));
        register("sethome",    new HomeCommands.SetHome(core));
        register("removehome", new HomeCommands.RemoveHome(core));

        // Economy
        register("balance", new EconomyCommands.Balance(core));
        register("deposit", new EconomyCommands.Deposit(core));
        register("take",    new EconomyCommands.Take(core));

        // Chat
        register("msg", new ChatCommands.Msg(core));

        // Misc
        register("pvp",     new MiscCommands.Pvp(core));
        register("upgrade", new MiscCommands.Upgrade(core));

        // Requests
        RequestCommands.Accept accept = new RequestCommands.Accept(core, commands);
        register("accept", accept);
        register("a",      accept);
        RequestCommands.Deny deny = new RequestCommands.Deny(core);
        register("deny", deny);
        register("d",    deny);

        this.reference = new ReferenceCommand(core);
    }

    private void register(String name, IClanCommand handler) {
        commands.put(name, handler);
    }

    /**
     * Entry point for the /clan command.
     * Resolves the subcommand, validates, then executes it.
     * Falls back to the reference/help page for unknown or absent subcommands.
     *
     * @param sender  the command sender
     * @param command the command object
     * @param label   the alias used
     * @param args    command arguments; {@code args[0]} is the subcommand name
     * @return {@code true} always
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String userName = sender.getName();
        Clan userClan = core.getClanList().getClanByName(userName);

        if (args.length == 0) {
            reference.show(sender, args, userClan, userName);
            return true;
        }

        IClanCommand cmd = commands.get(args[0].toLowerCase());
        if (cmd == null) {
            reference.show(sender, args, userClan, userName);
            return true;
        }

        if (cmd.validate(sender, args, userClan, userName)) {
            cmd.execute(sender, args, userClan, userName);
        }
        return true;
    }
}
