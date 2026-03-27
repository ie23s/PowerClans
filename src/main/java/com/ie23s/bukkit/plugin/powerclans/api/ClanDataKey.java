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
    /** Clan display tag shown in chat (colour-coded, e.g. {@code &cWAR}). */
    TAG("tag",                ""),
    /** Serialised home location string, or {@code null} if not set. */
    HOME("home",              null),
    /** Whether friendly-fire protection is enabled ({@code true} = protected). */
    PVP("pvp",                true),
    /** Clan bank balance. */
    BALANCE("balance",        0.0),
    /** Total mob kills accumulated by clan members. */
    MOB_KILLS("mob_kills",    0),
    /** Total player kills accumulated by clan members. */
    PLAYER_KILLS("player_kills", 0),
    /** Total online time in seconds accumulated by clan members. */
    ONLINE_TIME("online_time",   0);

    private final String ident;
    private final Object defaultValue;

    ClanDataKey(String ident, Object defaultValue) {
        this.ident        = ident;
        this.defaultValue = defaultValue;
    }

    /**
     * The string identifier stored in the {@code ident} column of {@code clan_data}.
     * @return the ident string
     */
    public String ident() {
        return ident;
    }

    /**
     * The default Java value used when this key has not been persisted yet.
     * @return the default value (may be {@code null} for {@link #HOME})
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
