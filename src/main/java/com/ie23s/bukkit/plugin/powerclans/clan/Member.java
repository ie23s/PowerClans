package com.ie23s.bukkit.plugin.powerclans.clan;

public class Member {
    private final String name;
    private boolean isModer;
    private final String clan;


    public Member(String name, boolean isModer, String clan) {
        this.name = name.toLowerCase();
        this.isModer = isModer;
        this.clan = clan;
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
