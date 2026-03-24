package com.ie23s.bukkit.plugin.powerclans.database;

import java.sql.Connection;

/**
 * Provides an active database connection, handling reconnection transparently.
 */
public interface ConnectionProvider {
    /**
     * Returns an active connection from the underlying pool.
     * The caller is responsible for closing it (preferably via try-with-resources),
     * which returns it to the pool rather than physically closing it.
     *
     * @return an active {@link Connection}
     * @throws DatabaseException if a connection cannot be acquired
     */
    Connection getConnection();
}
