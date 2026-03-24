package com.ie23s.bukkit.plugin.powerclans.command;

import com.ie23s.bukkit.plugin.powerclans.Core;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PowerClansCommand implements CommandExecutor {
    private final Core core;

    public PowerClansCommand(Core core) {
        this.core = core;
    }

    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (args.length == 0) {
            reference(sender, args);
            return true;
        }

        if (!sender.hasPermission("PowerClans.admin" + args[0])) {
            sender.sendMessage(core.lang("errors._1"));
            return false;
        }


        if (args[0].equalsIgnoreCase("reload")) {
            try {
                core.load();
            } catch (Exception e) {
                sender.sendMessage(core.lang("error._45"));
                core.getUtils().getLogger().error(e);
                return true;
            }
            sender.sendMessage(core.lang("command.reload"));
            return true;
        }
        reference(sender, args);
        return true;
    }

    private void reference(CommandSender sender, String[] args) {
        ArrayList<String> commands = new ArrayList<>();

        if (sender.hasPermission("PowerClans.admin.reload"))
            commands.add("reload");

        int page = 1;
        try {
            page = Integer.parseInt(args[0]);
        } catch (Exception ignore) {
        }
        sender.sendMessage(core.lang("reference._1"));
        for (int i = (page - 1) * 5; i < page * 5 && i < commands.size(); i++) {
            sender.sendMessage(core.lang("reference.startpc") + core.lang("reference.pc_" + commands.get(i)));
        }
        if (page * 5 <= commands.size()) {
            sender.sendMessage(core.lang("reference._2", (page + 1)));
        }
    }

}
