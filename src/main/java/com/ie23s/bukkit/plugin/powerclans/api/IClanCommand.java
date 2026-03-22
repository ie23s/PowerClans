package com.ie23s.bukkit.plugin.powerclans.api;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.command.CommandSender;

/**
 * Contract for a single /clan subcommand handler.
 * Each implementation is responsible for one command (e.g. create, disband, invite).
 */
public interface IClanCommand {

    /**
     * Validates whether the sender is allowed to run this command.
     * Sends error messages to the sender if validation fails.
     *
     * @param sender   the command sender
     * @param args     full command arguments; {@code args[0]} is the subcommand name
     * @param userClan the clan the sender currently belongs to, or {@code null} if none
     * @param userName the sender's player name
     * @return {@code true} if the command may proceed, {@code false} otherwise
     */
    boolean validate(CommandSender sender, String[] args, Clan userClan, String userName);

    /**
     * Executes the command. Called only after {@link #validate} returns {@code true}.
     *
     * @param sender   the command sender
     * @param args     full command arguments
     * @param userClan the clan the sender currently belongs to, or {@code null} if none
     * @param userName the sender's player name
     */
    void execute(CommandSender sender, String[] args, Clan userClan, String userName);
}
