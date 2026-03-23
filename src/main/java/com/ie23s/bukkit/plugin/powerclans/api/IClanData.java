package com.ie23s.bukkit.plugin.powerclans.api;

/**
 * Typed read/write access to the {@code clan_data} fields stored in a {@link ClanDataKey}-keyed map.
 * Default methods fall back to {@link ClanDataKey#defaultValue()} when a key is absent or wrong type.
 */
public interface IClanData {

    Object getRaw(ClanDataKey key);

    void set(ClanDataKey key, Object value);

    default int getInt(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof Number n ? n.intValue() : (Integer) key.defaultValue();
    }

    default String getString(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof String s ? s : (String) key.defaultValue();
    }

    default double getDouble(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof Number n ? n.doubleValue() : (Double) key.defaultValue();
    }

    default boolean getBoolean(ClanDataKey key) {
        Object val = getRaw(key);
        return val instanceof Boolean b ? b : (Boolean) key.defaultValue();
    }
}
