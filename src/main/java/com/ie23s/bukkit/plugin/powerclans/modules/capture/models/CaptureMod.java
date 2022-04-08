package com.ie23s.bukkit.plugin.powerclans.modules.capture.models;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;

import java.util.Date;

public class CaptureMod {

    private final int regionId;
    private final Date startDate;

    private final Clan attacker;
    private final Clan saver;

    private final boolean active = false;

    public CaptureMod(int regionId, Date startDate, Clan attacker, Clan saver) {
        this.regionId = regionId;
        this.startDate = startDate;
        this.attacker = attacker;
        this.saver = saver;
    }
}
