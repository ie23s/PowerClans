package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Represents a pending confirmation request sent to a player.
 * Stored in a global list; consumed when the player runs {@code /clan accept} or {@code /clan deny}.
 */
public class Request {

    public static ArrayList<Request> requests = new ArrayList<>();

    private Clan clan;
    private final Player player;
    private final String sender;
    private final long time;
    private final RequestType type;
    private String[] args;

    /**
     * Creates a request without a clan (used for clan creation).
     *
     * @param player the target player
     * @param sender the initiating player's name
     * @param type   the request type
     * @param args   the original command arguments
     */
    public Request(Player player, String sender, RequestType type, String[] args) {
        this.player = player;
        this.sender = sender;
        this.time = System.currentTimeMillis();
        this.type = type;
        this.args = args;
    }

    /**
     * Creates a request associated with a clan, without arguments.
     *
     * @param clan   the relevant clan
     * @param player the target player
     * @param sender the initiating player's name
     * @param type   the request type
     */
    public Request(Clan clan, Player player, String sender, RequestType type) {
        this.clan = clan;
        this.player = player;
        this.sender = sender;
        this.time = System.currentTimeMillis();
        this.type = type;
    }

    /**
     * Creates a request associated with a clan, with arguments.
     *
     * @param clan   the relevant clan
     * @param player the target player
     * @param sender the initiating player's name
     * @param type   the request type
     * @param args   the original command arguments
     */
    public Request(Clan clan, Player player, String sender, RequestType type, String[] args) {
        this.clan = clan;
        this.player = player;
        this.sender = sender;
        this.time = System.currentTimeMillis();
        this.type = type;
        this.args = args;
    }

    /**
     * Returns the pending request for the given player, or {@code null} if none exists.
     *
     * @param pl the player to look up
     * @return the pending {@link Request}, or {@code null}
     */
    public static Request get(Player pl) {
        for (Request req : requests) {
            if (req.getPlayer().equals(pl)) {
                return req;
            }
        }
        return null;
    }

    /** @return the clan associated with this request, or {@code null} */
    public Clan getClan() { return this.clan; }

    /** @return the player this request was sent to */
    public Player getPlayer() { return this.player; }

    /** @return the name of the player who initiated this request */
    public String getSender() { return this.sender; }

    /** @return the timestamp when this request was created */
    public long getTime() { return this.time; }

    /** @return the type of this request */
    public RequestType getType() { return this.type; }

    /** @return the original command arguments, or {@code null} if not stored */
    public String[] getArgs() { return args; }

    /**
     * Sends this request to the player.
     * Any existing pending request for the same player is replaced.
     */
    public void send() {
        Request existing = get(this.player);
        if (existing != null) existing.remove();
        requests.add(this);
    }

    /** Removes this request from the global pending list. */
    public void remove() {
        requests.remove(this);
    }
}
