package com.ie23s.bukkit.plugin.powerclans.database;

/**
 * Thrown when a database connection cannot be established or a query fails unexpectedly.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
