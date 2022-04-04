package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.database.InitDB;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Objects;

public class ClanList {
    private final HashMap<String, Clan> clans = new HashMap<>();
    private final InitDB db;
    private final Core core;

    public ClanList(Core core) {
        this.core = core;
        this.db = core.getDb();
    }

    public Clan getClan(String clan) {
        return clans.get(clan.toLowerCase());
    }

    public Clan getClanByName(String player) {
        if (core.getMemberList().isMember(player)) {
            return getClan(Objects.requireNonNull(core.getMemberList().getMember(player)).getClan());
        }

        return null;
    }

    public Clan create(String clan, String leader) {
        Member member = new Member(leader.toLowerCase(), false, clan);
        Clan c = new Clan(core, ChatColor.stripColor(clan.replaceAll("&", "§")), clan, leader.toLowerCase(), "none", core.getConfig().getInt("settings.default_max"), true, 0, 0, 0, 0, 1);
        clans.put(clan.toLowerCase(), c);
        core.getMemberList().addMember(member);
        db.createClan(c);
        db.createClanMember(member);
        return c;
    }

    public int number() {
        return clans.size();
    }

    public HashMap<String, Clan> getClans() {
        return clans;
    }

}
