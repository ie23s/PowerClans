package com.ie23s.bukkit.plugin.powerclans.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationSerializerTest {

    @Test
    void serialize_producesExpectedFormat() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location location = new Location(world, 1.5, 64.0, -3.5, 90.0f, 45.0f);

        assertEquals("world;1.5;64.0;-3.5;90.0;45.0", LocationSerializer.serialize(location));
    }

    @Test
    void deserialize_null_returnsNull() {
        assertNull(LocationSerializer.deserialize(null));
    }

    @Test
    void deserialize_malformed_throws() {
        assertThrows(IllegalArgumentException.class, () -> LocationSerializer.deserialize("notavalidlocation"));
        assertThrows(IllegalArgumentException.class, () -> LocationSerializer.deserialize("world;1;2;3"));
        assertThrows(IllegalArgumentException.class, () -> LocationSerializer.deserialize("world;x;y;z;0;0"));
    }

    @Test
    void deserialize_parsesAllFields() {
        World world = mock(World.class);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            Location loc = LocationSerializer.deserialize("world;1.5;64.0;-3.5;90.0;45.0");

            assertNotNull(loc);
            assertSame(world, loc.getWorld());
            assertEquals(1.5,  loc.getX());
            assertEquals(64.0, loc.getY());
            assertEquals(-3.5, loc.getZ());
            assertEquals(90.0f, loc.getYaw());
            assertEquals(45.0f, loc.getPitch());
        }
    }

    @Test
    void roundTrip_serializeDeserialize() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location original = new Location(world, 100.0, 70.0, 200.0, 180.0f, -10.0f);

        String serialized = LocationSerializer.serialize(original);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            Location restored = LocationSerializer.deserialize(serialized);

            assertNotNull(restored);
            assertEquals(original.getX(),     restored.getX());
            assertEquals(original.getY(),     restored.getY());
            assertEquals(original.getZ(),     restored.getZ());
            assertEquals(original.getYaw(),   restored.getYaw());
            assertEquals(original.getPitch(), restored.getPitch());
        }
    }
}
