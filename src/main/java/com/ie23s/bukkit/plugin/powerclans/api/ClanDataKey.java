package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Identifies each field stored as a row in the {@code clan_data} EAV table.
 *
 * <p>Each constant carries:
 * <ul>
 *   <li>{@link #ident()} — the string identifier used as the {@code ident} column value in the DB</li>
 *   <li>{@link #defaultValue()} — the Java value to use when the field has not been set</li>
 * </ul>
 *
 * <p>The type of {@link #defaultValue()} determines how {@link #parse(String)} deserialises
 * values read from the database:
 * {@link Boolean} → {@link Boolean#parseBoolean}, {@link Double} → {@link Double#parseDouble},
 * {@link Integer} → {@link Integer#parseInt}, {@link String} → returned as-is.
 */
public enum ClanDataKey {
    TAG("tag",                ""),
    HOME("home",              "none"),
    PVP("pvp",                true),
    BALANCE("balance",        0.0),
    MOB_KILLS("mob_kills",    0),
    PLAYER_KILLS("player_kills", 0),
    ONLINE_TIME("online_time",   0);

    private final String ident;
    private final Object defaultValue;

    ClanDataKey(String ident, Object defaultValue) {
        this.ident        = ident;
        this.defaultValue = defaultValue;
    }

    /**
     * The string identifier stored in the {@code ident} column of {@code clan_data}.
     */
    public String ident() {
        return ident;
    }

    /**
     * The default Java value used when this key has not been persisted yet.
     */
    public Object defaultValue() {
        return defaultValue;
    }

    /**
     * Finds the key whose {@link #ident()} equals the given string.
     *
     * @param ident the identifier to look up
     * @return the matching key
     * @throws IllegalArgumentException if no key matches
     */
    public static ClanDataKey fromIdent(String ident) {
        for (ClanDataKey key : values()) {
            if (key.ident.equals(ident)) return key;
        }
        throw new IllegalArgumentException("Unknown ClanDataKey ident: " + ident);
    }

    /**
     * Parses a string value read from the database into the correct Java type,
     * based on the type of {@link #defaultValue()}.
     *
     * @param value string-serialised value from the {@code value} column
     * @return typed Java object
     */
    public Object parse(String value) {
        if (defaultValue instanceof Boolean) return Boolean.parseBoolean(value);
        if (defaultValue instanceof Double)  return Double.parseDouble(value);
        if (defaultValue instanceof Integer) return Integer.parseInt(value);
        return value;
    }
}
