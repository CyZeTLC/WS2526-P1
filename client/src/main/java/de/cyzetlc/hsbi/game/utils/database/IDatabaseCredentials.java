package de.cyzetlc.hsbi.game.utils.database;

public interface IDatabaseCredentials {
    String getUsername();

    String getPassword();

    String getHostName();

    String getDatabase();

    int getPort();

    int getPoolSize();
}
