package com.ie23s.bukkit.plugin.powerclans.clan;

import com.ie23s.bukkit.plugin.powerclans.api.IMember;

public class Member implements IMember {

    private final String name;
    private boolean isModer;
    private final String clan;

    public Member(String name, boolean isModer, String clan) {
        this.name = name.toLowerCase();
        this.isModer = isModer;
        this.clan = clan;
    }

    @Override public String  getName()   { return name; }
    @Override public boolean isModer()   { return isModer; }
    @Override public String  getClan()   { return clan; }

    void setModer(boolean isModer) { this.isModer = isModer; }
}
