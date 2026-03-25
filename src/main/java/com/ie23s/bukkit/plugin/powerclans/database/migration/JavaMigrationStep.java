package com.ie23s.bukkit.plugin.powerclans.database.migration;

import com.ie23s.bukkit.plugin.powerclans.database.ConnectionProvider;

import java.sql.SQLException;

/**
 * A Java-based migration step executed after the corresponding SQL script.
 *
 * <p>Use for migrations that require data transformations that cannot be expressed in
 * SQL alone — e.g. resolving player UUIDs via the Bukkit API and writing them back to
 * the database.
 */
@FunctionalInterface
public interface JavaMigrationStep {

    /**
     * Performs the migration logic using a fresh connection from the given provider.
     *
     * @param provider connection provider for the current database
     * @throws SQLException if a database error occurs during the step
     */
    void run(ConnectionProvider provider) throws SQLException;
}
