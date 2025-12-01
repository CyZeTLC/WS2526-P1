package de.cyzetlc.hsbi.game.utils.json.database.mysql;

import de.cyzetlc.hsbi.game.utils.json.database.IDatabaseCredentials;

public class MySQLCredentials implements IDatabaseCredentials {
    public String username;
    public String password;
    public String hostname;
    public String database;
    public int port;
    public int poolSize;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getHostName() {
        return hostname;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }
}
