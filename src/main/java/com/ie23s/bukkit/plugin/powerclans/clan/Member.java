package com.ie23s.bukkit.plugin.powerclans.clan;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Member {

    private static HashMap<String, Member> members = new HashMap<>();
    private String name;
    private boolean isModer;
    private String clan;


    public Member(String name, boolean isModer, String clan) {
        this.name = name.toLowerCase();
        this.isModer = isModer;
        this.clan = clan;
    }

    static Member getMember(String name) {
        return members.get(name.toLowerCase());
    }

    public static boolean isMember(String name) {
        return members.containsKey(name.toLowerCase());
    }

    public static void addMember(Member member) {
        members.put(member.name.toLowerCase(), member);
    }

    static void removeMember(String name) {
        members.remove(name.toLowerCase());
    }

    public static ArrayList<String> getListOfMembers(String name) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry member : members.entrySet()) {
            if (((Member) member.getValue()).getClan().equalsIgnoreCase(name)) {
                list.add(((Member) member.getValue()).getName());
            }
        }
        return list;
    }

    public String getName() {
        return this.name;
    }

    public boolean isModer() {
        return this.isModer;
    }

    void setModer(boolean isModer) {
        this.isModer = isModer;
    }

    public String getClan() {
        return this.clan;
    }
}
