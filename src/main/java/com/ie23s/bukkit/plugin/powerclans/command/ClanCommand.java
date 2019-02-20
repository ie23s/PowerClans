package com.ie23s.bukkit.plugin.powerclans.command;

import com.ie23s.bukkit.plugin.powerclans.Main;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.clan.Member;
import com.ie23s.bukkit.plugin.powerclans.configuration.Configuration;
import com.ie23s.bukkit.plugin.powerclans.configuration.Language;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.Warm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


@SuppressWarnings("deprecation")
public class ClanCommand implements CommandExecutor {
    private String userName;
    private Clan userClan;
    private CommandSender sender;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.userName = sender.getName();
        this.userClan = Clan.getClanByName(userName);
        this.sender = sender;

        if (args.length == 0) return true;

        if (canUse(args)) {
            switch (args[0].toLowerCase()) {
                case "create":
                    new Request((Player) this.sender, this.userName, 1, args).send();
                    sender.sendMessage(Language.getMessage("clan.create_request", args[1]));
                    if (Configuration.getConfiguration().getInt("settings.create_cost") != 0 && !sender.hasPermission("PowerClans.free.create")) {
                        try {
                            //noinspection deprecation
                            if (!Main.getVault().has(sender.getName(), (double) Configuration.getConfiguration().getInt("settings.create_cost"))) {
                                sender.sendMessage(Language.getMessage("create_request_cost", args[1]));
                                return false;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    sender.sendMessage(Language.getMessage("command.request"));
                    return true;
                case "disband":
                    sender.sendMessage(Language.getMessage("clan.disband_request"));
                    new Request(this.userClan, (Player) this.sender, this.userName, 2, args).send();
                    sender.sendMessage(Language.getMessage("command.request"));
                    return true;
                case "msg":
                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 1; i < args.length; ++i) {
                        stringBuilder.append(args[i]).append(" ");
                    }

                    if (stringBuilder.toString().length() <= 3) {
                        sender.sendMessage(Language.getMessage("errors._40"));
                        return true;
                    } else {
                        ChatColor color = ChatColor.AQUA;
                        if (userClan.hasModer(sender.getName())) {
                            color = ChatColor.GREEN;
                        }

                        if (userClan.hasLeader(sender.getName())) {
                            color = ChatColor.GOLD;
                        }
                        userClan.broadcast(Language.getMessage("command.msg_format", Language.getMessage("command.msg_1"), color + sender.getName(), stringBuilder.toString()));

                    }
                    return true;
                case "online":
                    sender.sendMessage(Language.getMessage("command.online_1"));

                    for (String member : Member.getListOfMembers(userClan.getName())) {
                        if (Bukkit.getOfflinePlayer(member).isOnline()) {
                            if (sender.getName().equalsIgnoreCase(member)) {
                                sender.sendMessage(ChatColor.YELLOW + " > " + ChatColor.GREEN + member);
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + " - " + member);
                            }
                        }
                    }

                    return true;
                case "info":
                    sender.sendMessage(Language.getMessage("command.info_1", userClan.getName(), Member.getListOfMembers(userClan.getName()).size(), userClan.getMaxPlayers()));
                    sender.sendMessage(Language.getMessage("command.info_2", Bukkit.getOfflinePlayer(userClan.getLeader()).getName()));
                    return true;
                case "addmoder":
                    userClan.setModer(args[1], true);
                    userClan.broadcast(Language.getMessage("clan.addmoder", Bukkit.getOfflinePlayer(args[1]).getName()));
                    return true;
                case "delmoder":

                    userClan.setModer(args[1], false);
                    userClan.broadcast(Language.getMessage("clan.delmoder", Bukkit.getOfflinePlayer(args[1]).getName()));
                    return true;
                case "invite":
                    Request request = new Request(userClan, Bukkit.getPlayer(args[1]), sender.getName(), 0);
                    request.send();
                    sender.sendMessage(Language.getMessage("clan.invite", sender.getName(), args[1]));
                    Bukkit.getPlayer(args[1]).sendMessage(Language.getMessage("clan.invite", args[1], sender.getName()));
                    Bukkit.getPlayer(args[1]).sendMessage(Language.getMessage("clan.invite_accept"));
                    Bukkit.getPlayer(args[1]).sendMessage(Language.getMessage("clan.invite_deny"));
                    return true;
                case "kick":
                    userClan.kick(args[1]);
                    userClan.broadcast(Language.getMessage("clan.kick_1", Bukkit.getPlayer(args[1]).getName()));
                    Bukkit.getPlayer(args[1]).sendMessage(Language.getMessage("clan.kick_2"));
                    return true;
                case "list":
                    int var1;
                    int var2;
                    if (args.length > 1) {
                        try {
                            if (Integer.parseInt(args[1]) < 1) {
                                throw new Exception();
                            }

                            var1 = Integer.parseInt(args[1]);
                        } catch (Exception var19) {
                            sender.sendMessage(Language.getMessage("error._42"));
                            return true;
                        }

                        var1 = (var1 - 1) * 10;
                        if (Clan.number() - var1 < 0) {
                            sender.sendMessage(Language.getMessage("error._43"));
                            return true;
                        }
                    }

                    var1 = 0;
                    sender.sendMessage(Language.getMessage("clan.list", (int) Math.ceil((double) var1 / 10.0D) + 1, (int) Math.ceil((double) Clan.number() / 10.0D)));

                    for (var2 = var1; var2 < Clan.number() && var1 != 10; ++var2) {
                        ++var1;
                        Clan var6 = (Clan) Clan.getClans().values().toArray()[var2];
                        sender.sendMessage(ChatColor.YELLOW + " - " + var6.getName() + ChatColor.YELLOW + " [" + Member.getListOfMembers(var6.getName()).size() + "] (" + Bukkit.getOfflinePlayer(var6.getLeader()).getName() + ")");
                    }

                    return true;
                case "leave":
                    sender.sendMessage(Language.getMessage("clan.leave_request"));
                    new Request(this.userClan, (Player) this.sender, this.userName, 3, args).send();
                    sender.sendMessage(Language.getMessage("command.request"));
                    return true;
                case "home":
                    Warm.addPlayer((Player) sender, userClan);
                    return true;
                case "removehome":
                    userClan.removeHome();
                    userClan.broadcast(Language.getMessage("clan.removehome", sender.getName()));
                    return true;
                case "sethome":
                    Player player = (Player) sender;
                    userClan.setHome(player.getLocation());
                    userClan.broadcast(Language.getMessage("clan.sethome", sender.getName()));
                    return true;
                case "leader":
                    sender.sendMessage(Language.getMessage("clan.leader_request"));
                    new Request(this.userClan, (Player) this.sender, this.userName, 4, args).send();
                    sender.sendMessage(Language.getMessage("command.request"));
                    return true;
                case "take":
                    int var3;

                    try {
                        var3 = Integer.parseInt(args[1]);
                        if (var3 < 0) {
                            throw new Exception();
                        }
                    } catch (Exception var20) {
                        sender.sendMessage(Language.getMessage("clan.take_2"));
                        return true;
                    }

                    if (userClan.getBalance() < var3) {
                        sender.sendMessage(Language.getMessage("clan.take_3"));
                        return true;
                    } else {
                        try {
                            Main.getVault().depositPlayer(sender.getName(), (double) var3);
                        } catch (Exception ignore) {
                        }

                        userClan.setBalance(userClan.getBalance() - var3);
                        sender.sendMessage(Language.getMessage("clan.take_4", var3));
                        return true;
                    }

                case "balance":

                    sender.sendMessage(Language.getMessage("clan.balance", userClan.getBalance()));
                    return true;
                case "deposit":
                    int var4;
                    try {
                        var4 = Integer.parseInt(args[1]);
                        if (var4 < 0) {
                            throw new Exception();
                        }
                    } catch (Exception var21) {
                        sender.sendMessage(Language.getMessage("clan.deposit_2"));
                        return true;
                    }

                    try {
                        if (!Main.getVault().has(sender.getName(), (double) var4)) {
                            sender.sendMessage(Language.getMessage("clan.deposit_3"));
                            return true;
                        }
                    } catch (Exception ignore) {
                    }

                    try {
                        Main.getVault().withdrawPlayer(sender.getName(), (double) var4);
                        userClan.setBalance(userClan.getBalance() + var4);
                    } catch (Exception ignore) {
                    }

                    sender.sendMessage(Language.getMessage("clan.deposit_4", var4));
                    return true;
                case "upgrade":
                    if (Configuration.getConfiguration().getInt("settings.upgrade_cost") != 0) {
                        try {
                            if (userClan.getBalance() < Configuration.getConfiguration().getInt("settings.upgrade_cost")) {
                                sender.sendMessage(Language.getMessage("clan.take_3"));
                                return true;
                            }
                        } catch (Exception ignore) {
                        }

                        userClan.setBalance(userClan.getBalance() - Configuration.getConfiguration().getInt("settings.upgrade_cost"));
                    }
                    userClan.upgrade(1);
                    userClan.broadcast(Language.getMessage("clan.upgrade", sender.getName()));
                    return true;
                case "pvp":
                    userClan.setPvP(!userClan.isPvP());
                    userClan.broadcast(userClan.isPvP() ? Language.getMessage("clan.pvp_1", sender.getName()) : Language.getMessage("clan.pvp_2", sender.getName()));
                    return true;
                case "accept":
                case "a":

                    Request request1 = Request.get((Player) sender);
                    if (request1 == null) {
                        sender.sendMessage(Language.getMessage("errors._41"));
                        return true;
                    }

                    switch (request1.getType()) {
                        case 0:
                            request1.remove();
                            request1.getClan().broadcast(Language.getMessage("clan.join", userName));
                            request1.getClan().invite(userName);
                            sender.sendMessage(Language.getMessage("clan.invitation_accept"));
                            return true;
                        case 1:
                            String[] args1 = request1.getArgs();
                            if (!canUse(args1))
                                return true;
                            if (Configuration.getConfiguration().getInt("settings.create_cost") != 0 && !sender.hasPermission("PowerClans.free.create")) {
                                try {
                                    Main.getVault().withdrawPlayer(sender.getName(), (double) Configuration.getConfiguration().getInt("settings.create_cost"));
                                } catch (Exception ignore) {
                                }
                            }
                            userClan = Clan.create(args1[1], sender.getName());
                            userClan.broadcast(Language.getMessage("clan.created", Objects.requireNonNull(Clan.getClanByName(sender.getName())).getName()));
                            return true;
                        case 2:
                            String[] args2 = request1.getArgs();
                            if (!canUse(args2))
                                return true;
                            userClan.broadcast(Language.getMessage("clan.disband", userClan.getName()));
                            userClan.disband();
                            return true;
                        case 3:
                            String[] args3 = request1.getArgs();
                            if (!canUse(args3))
                                return true;
                            userClan.broadcast(Language.getMessage("clan.leave_2", sender.getName()));
                            userClan.kick(sender.getName());
                            return true;
                        case 4:
                            String[] args4 = request1.getArgs();
                            if (!canUse(args4))
                                return true;
                            if (userClan.hasModer(args4[1])) {
                                userClan.setModer(args4[1], false);
                            }

                            userClan.setLeader(args4[1]);
                            userClan.broadcast(Language.getMessage("clan.leader", sender.getName(), args4[1]));
                            return true;
                    }

                    return true;
                case "deny":
                case "d":
                    Request request2 = Request.get((Player) sender);
                    if (request2 == null) {
                        sender.sendMessage(Language.getMessage("errors._41"));
                        return true;
                    }

                    request2.remove();
                    sender.sendMessage(Language.getMessage("command.deny"));
                    return true;
                case "top":
                    HashMap<Clan, Integer> sorted = new HashMap<>();

                    for (Clan entries : Clan.getClans().values()) {
                        sorted.put(entries, Member.getListOfMembers(entries.getName()).size());
                    }

                    LinkedList<Map.Entry<Clan, Integer>> var24 = new LinkedList<>(sorted.entrySet());
                    var24.sort((Comparator<Map.Entry>) (o1, o2) -> ((Integer) o2.getValue()).compareTo((Integer) o1.getValue()));
                    int var5 = 1;

                    for (Iterator<Map.Entry<Clan, Integer>> var11 = var24.iterator(); var11.hasNext(); ++var5) {
                        Map.Entry<Clan, Integer> entry = var11.next();
                        Clan c = entry.getKey();
                        sender.sendMessage(Language.getMessage("command.top_1", var5, c.getName(), c.getLeader(), entry.getValue()));
                        if (var5 == 10) {
                            break;
                        }
                    }

                    return true;
                default:
                    reference(sender, args);

            }
        }

        return true;
    }

    private boolean canUse(String[] args) {
        if (args.length == 0) return false;
        if (!sender.hasPermission("PowerClans." + args[0])) {
            sender.sendMessage(Language.getMessage("errors._1"));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                String newClanName;
                if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("errors._2"));
                    return false;
                } else if (userClan != null) {
                    sender.sendMessage(Language.getMessage("errors._3"));
                    return false;
                } else if (Clan.getClan(newClanName = ChatColor.stripColor(args[1].replaceAll("&", "§"))) != null) {
                    sender.sendMessage(Language.getMessage("errors._4"));
                    return false;
                } else if (newClanName.length() > Configuration.getConfiguration().getInt("settings.max_symbols")) {
                    sender.sendMessage(Language.getMessage("errors._5", Configuration.getConfiguration().getInt("settings.max_symbols")));
                    return false;
                } else if (newClanName.length() < Configuration.getConfiguration().getInt("settings.min_symbols")) {
                    sender.sendMessage(Language.getMessage("errors._6", Configuration.getConfiguration().getInt("settings.min_symbols")));
                    return false;
                } else if (!newClanName.matches(Configuration.getConfiguration().getString("settings.clan_regex"))) {
                    sender.sendMessage(Language.getMessage("errors._7"));
                    return false;
                }

                if (Configuration.getConfiguration().getInt("settings.create_cost") != 0 && !sender.hasPermission("PowerClans.free.create")) {
                    try {
                        //noinspection deprecation
                        if (!Main.getVault().has(sender.getName(), (double) Configuration.getConfiguration().getInt("settings.create_cost"))) {
                            sender.sendMessage(Language.getMessage("errors._8"));
                            return false;
                        }
                    } catch (Exception ignored) {
                    }
                }
                return true;
            case "disband":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._10"));
                    return false;
                }
                return true;
            case "msg":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName()) && !userClan.hasModer(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._11"));
                    return false;
                }
                return true;
            case "online":
            case "info":
            case "balance":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                }
                return true;
            case "addmoder":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("errors._12"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._13"));
                    return false;
                } else if (!userClan.hasClanMember(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._14"));
                    return false;
                } else if (userClan.hasLeader(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._15"));
                    return false;
                } else if (userClan.hasModer(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._16"));
                    return false;
                }
                return true;
            case "delmoder":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("errors._12"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._17"));
                    return false;
                } else if (!userClan.hasClanMember(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._14"));
                    return false;
                } else if (!userClan.hasModer(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._18"));
                    return false;
                }
                return true;
            case "invite":
                if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("errors._12"));
                    return false;
                } else if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName()) && !userClan.hasModer(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._19"));
                    return false;
                } else if (!Bukkit.getOfflinePlayer(args[1]).isOnline()) {
                    sender.sendMessage(Language.getMessage("errors._20"));
                    return false;
                } else if (Member.isMember(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._21"));
                    return false;
                } else if (userClan.getMaxPlayers() <= Member.getListOfMembers(userClan.getName()).size()) {
                    sender.sendMessage(Language.getMessage("errors._22", userClan.getMaxPlayers()));
                    return false;
                } else if (Request.get(Bukkit.getPlayer(args[1])) != null) {
                    sender.sendMessage(Language.getMessage("error.23"));
                    return false;
                }
                return true;
            case "kick":
                if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("errors._12"));
                    return false;
                } else if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName()) && !userClan.hasModer(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._24"));
                    return false;
                } else if (!userClan.hasClanMember(args[1])) {
                    sender.sendMessage(Language.getMessage("errors._14"));
                    return false;
                } else if (args[1].equalsIgnoreCase(userClan.getLeader())) {
                    sender.sendMessage(Language.getMessage("errors._25"));
                    return false;
                }
                return true;
            case "list":
                if (Clan.number() == 0) {
                    sender.sendMessage(Language.getMessage("errors._26"));
                    return false;
                }
                return true;
            case "leave":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (userClan.hasLeader(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._27"));
                    return false;
                }
                return true;
            case "home":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasHome()) {
                    sender.sendMessage(Language.getMessage("errors._28"));
                    return false;
                }
                return true;
            case "removehome":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(userName)) {
                    sender.sendMessage(Language.getMessage("errors._29"));
                    return false;
                } else if (!userClan.hasHome()) {
                    sender.sendMessage(Language.getMessage("errors._28"));
                    return false;
                }
                return true;
            case "sethome":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(userName)) {
                    sender.sendMessage(Language.getMessage("errors._30"));
                    return false;
                } else if (!Objects.requireNonNull(Main.getWG()).canBuild((Player) sender, ((Player) sender).getLocation())) {
                    sender.sendMessage(Language.getMessage("errors._31"));
                    return false;
                } else if (Member.getListOfMembers(userClan.getName()).size() < Configuration.getConfiguration().getInt("settings.home_min")) {
                    sender.sendMessage(Language.getMessage("errors._32", Configuration.getConfiguration().getInt("settings.home_min")));
                    return false;
                }
                return true;
            case "leader":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(userName)) {
                    sender.sendMessage(Language.getMessage("errors._36"));
                    return false;
                } else if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("errors._12"));
                    return false;
                } else if (!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
                    sender.sendMessage(Language.getMessage("errors._33"));
                    return false;
                } else if (Clan.getClanByName(args[1]) == null) {
                    sender.sendMessage(Language.getMessage("errors._14"));
                    return false;
                } else if (!Objects.requireNonNull(Clan.getClanByName(args[1])).getName().equalsIgnoreCase(userClan.getName())) {
                    sender.sendMessage(Language.getMessage("errors._34"));
                    return false;
                } else if (args[1].equalsIgnoreCase(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._35"));
                    return false;
                }
                return true;
            case "take":
                try {
                    Main.getVault();
                } catch (Exception var14) {
                    sender.sendMessage(Language.getMessage("errors._44"));
                    return false;
                }
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(userName)) {
                    sender.sendMessage(Language.getMessage("errors._36"));
                    return false;
                } else if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("clan.take_1"));
                    return false;
                }
                return true;

            case "deposit":
                try {
                    Main.getVault();
                } catch (Exception var14) {
                    sender.sendMessage(Language.getMessage("errors._44"));
                    return false;
                }
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (args.length == 1) {
                    sender.sendMessage(Language.getMessage("clan.deposit_1"));
                    return false;
                }
                return true;
            case "upgrade":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._37"));
                    return false;
                } else if (userClan.getMaxPlayers() >= Configuration.getConfiguration().getInt("settings.max_upgrade")) {
                    sender.sendMessage(Language.getMessage("errors._38"));
                    return false;
                }
                return true;
            case "top":
                if (Clan.number() == 0) {
                    sender.sendMessage(Language.getMessage("errors._26"));
                    return false;
                }
                return true;

            case "pvp":
                if (userClan == null) {
                    sender.sendMessage(Language.getMessage("errors._9"));
                    return false;
                } else if (!userClan.hasLeader(sender.getName()) && !userClan.hasModer(sender.getName())) {
                    sender.sendMessage(Language.getMessage("errors._39"));
                    return false;
                }
                return true;
            case "accept":
            case "a":
            case "cancel":
            case "c":
                return true;
        }
        return true;
    }

    private void reference(CommandSender sender, String[] args) {
        ArrayList<String> commands = new ArrayList<>();

        if ((sender instanceof Player)) {

            if (userClan == null) {
                if (sender.hasPermission("PowerClans.create"))
                    commands.add("create");
            } else {
                if (userClan.hasLeader(userName)) {
                    if (sender.hasPermission("PowerClans.disband"))
                        commands.add("disband");
                    if (sender.hasPermission("PowerClans.leader"))
                        commands.add("leader");
                    if (sender.hasPermission("PowerClans.addmoder"))
                        commands.add("addmoder");
                    if (sender.hasPermission("PowerClans.delmoder"))
                        commands.add("delmoder");
                    if (sender.hasPermission("PowerClans.take"))
                        commands.add("take");
                    if (sender.hasPermission("PowerClans.sethome"))
                        commands.add("sethome");
                    if (sender.hasPermission("PowerClans.removehome"))
                        commands.add("removehome");
                    if (sender.hasPermission("PowerClans.upgrade"))
                        commands.add("upgrade");
                    if (sender.hasPermission("PowerClans.disband"))
                        commands.add("disband");
                }

                if (userClan.hasModer(userName) || userClan.hasLeader(userName)) {
                    if (sender.hasPermission("PowerClans.msg"))
                        commands.add("msg");
                    if (sender.hasPermission("PowerClans.invite"))
                        commands.add("invite");
                    if (sender.hasPermission("PowerClans.kick"))
                        commands.add("kick");
                    if (sender.hasPermission("PowerClans.pvp"))
                        commands.add("pvp");

                }
                if (sender.hasPermission("PowerClans.info"))
                    commands.add("info");
                if (sender.hasPermission("PowerClans.online"))
                    commands.add("online");
                if (sender.hasPermission("PowerClans.home"))
                    commands.add("home");
                if (sender.hasPermission("PowerClans.balance"))
                    commands.add("balance");
                if (sender.hasPermission("PowerClans.deposit"))
                    commands.add("deposit");
            }

            if (sender.hasPermission("PowerClans.list"))
                commands.add("list");
            if (sender.hasPermission("PowerClans.top"))
                commands.add("top");
        }

        int page = 1;
        try {
            page = Integer.parseInt(args[0]);
        } catch (Exception ignore) {
        }
        sender.sendMessage(Language.getMessage("reference._1"));
        for (int i = (page - 1) * 5; i < page * 5 && i < commands.size(); i++) {
            sender.sendMessage(Language.getMessage("reference.start") + Language.getMessage("reference." + commands.get(i)));
        }
        if (page*5 <= commands.size()) {
            sender.sendMessage(Language.getMessage("reference._2", (page + 1)));
        }
    }
}
