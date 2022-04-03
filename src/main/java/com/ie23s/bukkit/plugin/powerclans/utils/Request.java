package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Request {

    public static ArrayList<Request> requests = new ArrayList<>();

    private Clan clan;
    private final Player player;
    private final String sender;
    private final long time;
    private final byte type;
    private String[] args;


    public Request(Player player, String sender, int type, String[] args) {
        this.player = player;
        this.sender = sender;
        this.time = System.currentTimeMillis();
        this.type = (byte) type;
        this.args = args;
    }

    public Request(Clan clan, Player player, String sender, int type) {
        this.clan = clan;
        this.player = player;
        this.sender = sender;
        this.time = System.currentTimeMillis();
        this.type = (byte) type;
    }

    public Request(Clan clan, Player player, String sender, int type, String[] args) {
        this.clan = clan;
        this.player = player;
        this.sender = sender;
        this.time = System.currentTimeMillis();
        this.type = (byte) type;
        this.args = args;
    }

    public static Request get(Player pl) {

        for (Request req : requests) {
            if (req.getPlayer().equals(pl)) {
                return req;
            }
        }

        return null;
    }

    public Clan getClan() {
        return this.clan;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getSender() {
        return this.sender;
    }

    public long getTime() {
        return this.time;
    }

    public byte getType() {
        return this.type;
    }

    public void send() {
        Request request = get(this.player);
        if (request != null)
            request.remove();
        requests.add(this);
    }

    public void remove() {
        requests.remove(this);
    }

    public String[] getArgs() {
        return args;
    }
}
