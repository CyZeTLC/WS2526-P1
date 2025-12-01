package de.cyzetlc.hsbi.game.utils.json.database.mysql;

import de.cyzetlc.hsbi.game.utils.json.database.IMySQLExtension;
import lombok.Getter;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Getter
public class MySQLQueryBuilder {
    // It's creating a new thread pool.
    private final ExecutorService executorService;
    // It's creating a new connection to the database.
    private final Connection connection;
    // It's creating a new connection to the database.
    private final IMySQLExtension extension;

    // It's creating a new RowSetFactory object.
    private RowSetFactory factory;
    // It's creating a new LinkedList object.
    private LinkedList<Object> params;
    // It's creating a new String object.
    private String query;

    public MySQLQueryBuilder(IMySQLExtension extension) {
        this.executorService = Executors.newFixedThreadPool(15);
        this.params = new LinkedList<>();
        this.extension = extension;
        this.connection = extension.getNewConnection();
        try {
            this.factory = RowSetProvider.newFactory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function executes a query synchronously and returns the result.
     *
     * @return A CachedRowSet
     */
    public CachedRowSet executeQuerySync() {
        return this.executeQueryOrUpdateSync(false);
    }

    /**
     * If the query is a SELECT, executeQuerySync() is called, otherwise executeUpdateSync() is called.
     *
     * @return A CachedRowSet object.
     */
    public CachedRowSet executeUpdateSync() {
        return this.executeQueryOrUpdateSync(true);
    }

    /**
     * "Execute a query asynchronously and call the callback when it's done."
     *
     * The first thing we do is check if the connection is closed. If it is, we throw an exception
     *
     * @param callback A Consumer<CachedRowSet> object that will be called when the query is finished.
     */
    public void executeQueryAsync(Consumer<CachedRowSet> callback) {
        this.executeQueryOrUpdateAsync(callback, false);
    }

    /**
     * "If the query is not already running, then run it asynchronously."
     *
     * The first thing the function does is check to see if the query is already running. If it is, then it returns
     * immediately
     */
    public void executeUpdateAsync() {
        this.executeQueryOrUpdateAsync(null, true);
    }

    /**
     * It executes a query or update statement asynchronously and calls the callback function with the result
     *
     * @param callback The callback to be executed when the query is finished.
     * @param useUpdateStatement If true, the query will be executed as an update statement. If false, it will be executed
     * as a query.
     */
    private void executeQueryOrUpdateAsync(Consumer<CachedRowSet> callback, boolean useUpdateStatement) {
        this.executorService.execute(() -> {
            CachedRowSet rs = MySQLQueryBuilder.this.executeQueryOrUpdateSync(useUpdateStatement);
            if (callback != null) {
                callback.accept(rs);
            }
        });
    }

    /**
     * It takes a query and a list of parameters, and returns a CachedRowSet
     *
     * @param useUpdateStatement If true, the query will be executed as an update statement.
     * @return A CachedRowSet
     */
    private CachedRowSet executeQueryOrUpdateSync(boolean useUpdateStatement) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        CachedRowSet var5;

        try {
            statement = this.connection.prepareStatement(this.query);

            for (int i = 0; i < this.params.size(); ++i) {
                statement.setObject(i + 1, this.params.get(i));
            }

            CachedRowSet crs;

            if (useUpdateStatement) {
                statement.executeUpdate();
                this.closeItems(null, statement);
                return null;
            }

            rs = statement.executeQuery();
            if (rs == null) {
                return null;
            }

            crs = factory.createCachedRowSet();
            crs.populate(rs);
            this.closeItems(rs, statement);
            var5 = crs;
        } catch (SQLException var17) {
            this.printDebugInformation();
            var17.printStackTrace();
            return null;
        } finally {
            try {
                this.closeItems(rs, statement);
            } catch (SQLException var16) {
                var16.printStackTrace();
            }

        }

        return var5;
    }

    /**
     * "Close all the things."
     *
     * The function is called from the `getAllItems` function, which is called from the `getItems` function
     *
     * @param rs The ResultSet object that is returned from the query.
     * @param statement The SQL statement to be executed.
     */
    private void closeItems(ResultSet rs, PreparedStatement statement) throws SQLException {
        if (rs != null) {
            rs.close();
        }

        if (this.connection != null) {
            this.connection.close();
        }

        if (statement != null) {
            statement.close();
        }

        this.extension.closeConnection(this.connection);
    }

    /**
     * It prints the query and the parameters to the console
     *
     * @return The MySQLQueryBuilder object.
     */
    public MySQLQueryBuilder printDebugInformation() {
        System.out.println("-----------------------------");
        System.out.println("Query - Debug");
        System.out.println("Query: " + this.query);
        System.out.println(" ");
        System.out.println("Parameters: ");

        for (Object param : this.params) {
            System.out.println("- " + param);
        }

        System.out.println("-----------------------------");
        return this;
    }

    /**
     * > This function sets the query string to the value of the parameter qry
     *
     * @param qry The query to be executed.
     * @return The object itself.
     */
    public MySQLQueryBuilder setQuery(String qry) {
        this.query = qry;
        return this;
    }

    /**
     * Add a parameter to the query.
     *
     * @param obj The object to be added to the list of parameters.
     * @return The MySQLQueryBuilder object
     */
    public MySQLQueryBuilder addParameter(Object obj) {
        this.params.add(obj);
        return this;
    }

    /**
     * Add all the objects in the collection to the list of parameters.
     *
     * @param objects The objects to add to the parameters list.
     * @return The MySQLQueryBuilder object.
     */
    public MySQLQueryBuilder addParameters(Collection<Object> objects) {
        this.params.addAll(objects);
        return this;
    }
}
