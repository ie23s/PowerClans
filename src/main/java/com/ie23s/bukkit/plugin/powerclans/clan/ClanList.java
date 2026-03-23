package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClanList {

    private final HashMap<String, Clan> clans = new HashMap<>();
    private final Core core;

    public ClanList(Core core) {
        this.core = core;
    }

    public Clan getClan(String clan) {
        return clans.get(clan.toLowerCase());
    }

    public Clan getClanByUuid(String uuid) {
        for (Clan clan : clans.values()) {
            if (clan.getUuid().equals(uuid)) return clan;
        }
        return null;
    }

    public Clan getClanByName(String player) {
        if (core.getMemberList().isMember(player)) {
            return getClan(Objects.requireNonNull(core.getMemberList().getMember(player)).getClan());
        }
        return null;
    }

    public Clan create(String clan, String leader) {
        String strippedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', clan));
        Member member = new Member(leader.toLowerCase(), false, strippedName);
        Clan c = new Clan(core, java.util.UUID.randomUUID().toString(), strippedName,
                leader.toLowerCase(), core.getConfig().getInt("settings.default_max"), 1);
        c.set(ClanDataKey.TAG, clan);
        clans.put(clan.toLowerCase(), c);
        core.getMemberList().addMember(member);
        core.getClanService().create(c);
        core.getClanDataService().create(c);
        core.getMemberService().create(member);
        return c;
    }

    public int number() {
        return clans.size();
    }

    public Map<String, Clan> getClans() {
        return clans;
    }
}
