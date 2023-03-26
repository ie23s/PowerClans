package com.ie23s.bukkit.plugin.powerclans.clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemberList {

    private final HashMap<String, Member> members = new HashMap<>();

    public boolean isMember(String name) {
        return members.containsKey(name.toLowerCase());
    }

    public void addMember(Member member) {
        members.put(member.getName().toLowerCase(), member);
    }

    public Member getMember(String name) {
        return members.get(name.toLowerCase());
    }

    void removeMember(String name) {
        members.remove(name.toLowerCase());
    }

    public ArrayList<String> getListOfMembers(String name) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry member : members.entrySet()) {
            if (((Member) member.getValue()).getClan().equalsIgnoreCase(name)) {
                list.add(((Member) member.getValue()).getName());
            }
        }
        return list;
    }
}
