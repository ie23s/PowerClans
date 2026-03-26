package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Typed read/write access to the {@code clan_data} fields stored in a {@link ClanDataKey}-keyed map.
 * Default methods fall back to {@link ClanDataKey#defaultValue()} when a key is absent or the stored
 * value is of an incompatible type.
 */
public interface IClanData {

    /**
     * Returns the raw stored value for the given key, or the key's default value if absent.
     *
     * @param key the data key to look up
     * @return the stored object, or {@code null} if the key's default is {@code null} and it has not been set
     */
    Object getRaw(ClanDataKey key);

    /**
     * Stores a value for the given key, replacing any previously stored value.
     *
     * @param key   the data key to set
     * @param value the value to store
     */
    void set(ClanDataKey key, Object value);

    /**
     * Returns the value for {@code key} as an {@code int}.
     * Falls back to the key's default if the stored value is not a {@link Number}.
     */
    default int getInt(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof Number n ? n.intValue() : (Integer) key.defaultValue();
    }

    /**
     * Returns the value for {@code key} as a {@link String}.
     * Falls back to the key's default if the stored value is not a {@code String}.
     */
    default String getString(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof String s ? s : (String) key.defaultValue();
    }

    /**
     * Returns the value for {@code key} as a {@code double}.
     * Falls back to the key's default if the stored value is not a {@link Number}.
     */
    default double getDouble(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof Number n ? n.doubleValue() : (Double) key.defaultValue();
    }

    /**
     * Returns the value for {@code key} as a {@code boolean}.
     * Falls back to the key's default if the stored value is not a {@link Boolean}.
     */
    default boolean getBoolean(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof Boolean b ? b : (Boolean) key.defaultValue();
    }
}
