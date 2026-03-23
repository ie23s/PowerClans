package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Identifies each field stored in the {@code clan_data} table.
 * Each constant carries the SQL column name and a default value used when the field
 * is not yet populated (e.g. for a freshly created clan).
 */
public enum ClanDataKey {
    TAG("tag", ""),
    HOME("home", "none"),
    PVP("pvp", true),
    BALANCE("balance", 0.0),
    MOB_KILLS("mob_kills", 0),
    PLAYER_KILLS("player_kills", 0),
    ONLINE_TIME("online_time", 0);

    private final String column;
    private final Object defaultValue;

    ClanDataKey(String column, Object defaultValue) {
        this.column = column;
        this.defaultValue = defaultValue;
    }

    /** SQL column name for this key. */
    public String column() {
        return column;
    }

    /** Value to use when this key has not been set explicitly. */
    public Object defaultValue() {
        return defaultValue;
    }
}
