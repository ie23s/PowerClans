package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.IClanCommand;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.command.CommandSender;

/**
 * Abstract base for all /clan command handlers.
 * Provides shared validation helpers and language shortcuts to avoid repetition.
 */
public abstract class BaseClanCommand implements IClanCommand {

    /** The plugin core instance available to all commands. */
    protected final Core core;

    /**
     * @param core the plugin core instance
     */
    protected BaseClanCommand(Core core) {
        this.core = core;
    }

    // -------------------------------------------------------------------------
    // Validation helpers — send an error message and return false on failure
    // -------------------------------------------------------------------------

    /**
     * Checks that the sender holds the {@code PowerClans.<subcommand>} permission.
     * Sends {@code errors._1} on failure.
     */
    protected boolean perm(CommandSender sender, String[] args) {
        if (sender.hasPermission("PowerClans." + args[0])) return true;
        sender.sendMessage(core.lang("errors._1"));
        return false;
    }

    /**
     * Checks that the sender is currently a member of a clan.
     * Sends {@code errors._9} on failure.
     */
    protected boolean inClan(CommandSender sender, Clan clan) {
        if (clan != null) return true;
        sender.sendMessage(core.lang("errors._9"));
        return false;
    }

    /**
     * Checks that a target argument ({@code args[1]}) is present.
     * Sends {@code errors._12} on failure.
     */
    protected boolean hasTarget(CommandSender sender, String[] args) {
        if (args.length >= 2) return true;
        sender.sendMessage(core.lang("errors._12"));
        return false;
    }

    /**
     * Checks that the sender is the clan leader.
     * Sends the given error key on failure.
     *
     * @param errorKey the language key to send on failure
     */
    protected boolean isLeader(CommandSender sender, Clan clan, String errorKey) {
        if (clan.hasLeader(sender.getName())) return true;
        sender.sendMessage(core.lang(errorKey));
        return false;
    }

    /**
     * Checks that the sender is either the clan leader or a moderator.
     * Sends the given error key on failure.
     *
     * @param errorKey the language key to send on failure
     */
    protected boolean isLeaderOrModer(CommandSender sender, Clan clan, String errorKey) {
        if (clan.hasLeader(sender.getName()) || clan.hasModer(sender.getName())) return true;
        sender.sendMessage(core.lang(errorKey));
        return false;
    }

    /**
     * Checks that the target player is a member of the clan.
     * Sends {@code errors._14} on failure.
     */
    protected boolean isClanMember(CommandSender sender, Clan clan, String target) {
        if (clan.hasClanMember(target)) return true;
        sender.sendMessage(core.lang("errors._14"));
        return false;
    }

    /**
     * Checks that Vault (economy) is available.
     * Sends {@code errors._44} on failure.
     */
    protected boolean vaultOk(CommandSender sender) {
        try {
            Core.getVault();
            return true;
        } catch (Exception e) {
            sender.sendMessage(core.lang("errors._44"));
            return false;
        }
    }

    /**
     * Parses a non-negative integer from the given string.
     * Sends the given error key and returns {@code -1} on failure.
     *
     * @param errorKey the language key to send on failure
     */
    protected int parsePositiveInt(CommandSender sender, String raw, String errorKey) {
        try {
            int val = Integer.parseInt(raw);
            if (val < 0) throw new NumberFormatException();
            return val;
        } catch (Exception e) {
            sender.sendMessage(core.lang(errorKey));
            return -1;
        }
    }

}
