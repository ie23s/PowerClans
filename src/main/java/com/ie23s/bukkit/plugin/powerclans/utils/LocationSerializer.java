package com.ie23s.bukkit.plugin.powerclans.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

/** Serialises and deserialises a Bukkit {@link Location} to/from a storable string. */
public final class LocationSerializer {

    private static final String DELIMITER = ";";

    private LocationSerializer() {}

    /**
     * Serialises a location to {@code "world;x;y;z;yaw;pitch"}.
     *
     * @param location location to serialise; must not be {@code null}
     * @return serialised string
     * @throws NullPointerException if {@code location} or its world is {@code null}
     */
    public static String serialize(Location location) {
        return Objects.requireNonNull(location.getWorld()).getName()
                + DELIMITER + location.getX()
                + DELIMITER + location.getY()
                + DELIMITER + location.getZ()
                + DELIMITER + location.getYaw()
                + DELIMITER + location.getPitch();
    }

    /**
     * Deserialises a string produced by {@link #serialize} back into a {@link Location}.
     *
     * @param value serialised string, or {@code null} if no home is set
     * @return the location, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null but not a valid serialised location
     */
    public static Location deserialize(String value) {
        if (value == null) return null;
        String[] p = value.split(DELIMITER, -1);
        if (p.length != 6) {
            throw new IllegalArgumentException("Malformed location string: " + value);
        }
        try {
            double x     = Double.parseDouble(p[1]);
            double y     = Double.parseDouble(p[2]);
            double z     = Double.parseDouble(p[3]);
            float  yaw   = Float.parseFloat(p[4]);
            float  pitch = Float.parseFloat(p[5]);
            return new Location(Bukkit.getWorld(p[0]), x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed location string: " + value, e);
        }
    }
}
