package de.cyzetlc.hsbi.game.utils.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cyzetlc.hsbi.game.utils.database.IDatabaseCredentials;
import de.cyzetlc.hsbi.game.utils.database.IMySQLExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryHandler implements IMySQLExtension {
    private HikariDataSource hikari;

    public QueryHandler(IDatabaseCredentials credentials) {
        HikariConfig config = this.getHikariConfig(credentials);

        try {
            this.hikari = new HikariDataSource(config);
            Connection connection = this.getNewConnection();
            String timeZone = null;
            ResultSet databaseTimeZone = connection.prepareStatement("SELECT @@GLOBAL.time_zone AS time_zone;").executeQuery();

            if (databaseTimeZone.next()) {
                timeZone = databaseTimeZone.getString("time_zone");
            }

            databaseTimeZone.close();
            connection.close();
            this.closeConnection(connection);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    private HikariConfig getHikariConfig(IDatabaseCredentials credentials) {
        HikariConfig config = new HikariConfig();
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());
        config.setConnectionTimeout(5000L);
        config.setMaximumPoolSize(credentials.getPoolSize());

        String jdbcConStr = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&verifyServerCertificate=false&allowPublicKeyRetrieval=true&characterEncoding=latin1",
                credentials.getHostName(), credentials.getPort(), credentials.getDatabase());

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcConStr);
        return config;
    }

    /**
     * It creates a new MySQLQueryBuilder object, sets the query, and returns it
     *
     * @param qry The query to be built.
     * @return A new instance of MySQLQueryBuilder
     */
    public MySQLQueryBuilder createBuilder(String qry) {
        return new MySQLQueryBuilder(this).setQuery(qry);
    }

    /**
     * Create a new MySQLQueryBuilder object, passing this MySQLQueryBuilderFactory object to the constructor.
     *
     * @return A new instance of the MySQLQueryBuilder class.
     */
    public MySQLQueryBuilder createBuilder() {
        return new MySQLQueryBuilder(this);
    }

    @Override
    // Getting a new connection from the HikariDataSource object.
    public Connection getNewConnection() {
        try {
            return this.hikari.getConnection();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        this.hikari.evictConnection(connection);
    }

    @Override
    public void stop() {
        if (!this.hikari.isClosed()) {
            this.hikari.close();
        }
    }
}
