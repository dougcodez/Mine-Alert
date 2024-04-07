package io.github.dougcodez.minealert.mysql.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class StatementAPI {

    private ConnectionType connectionType;

    public void setConnectionType(ConnectionType connectionType){
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    /**
     * Execute an SQL query with a result set and prepared statement
     *
     * @param sql SQL Statement
     * @param cfg Prepare statement configuration
     * @param op  read from result set
     * @param <R> Returns {R}
     * @return Result set
     * <pre>
     *     executeQuery("SELECT * FROM foo WHERE id = ?", (ps) -> ps.setInt(id, 69), rs -> {
     *        if(rs.next()) {
     *            ...
     *        }
     *     });
     * </pre>
     */
    public <R> R executeQuery(String sql, ThrowingConsumer<PreparedStatement> cfg, ThrowingFunction<ResultSet, R, SQLException> op) {
        return wrapException(sql, s -> {
            cfg.accept(s);
            return op.apply(s.executeQuery());
        });
    }

    /**
     * Executes an SQL update w/o result set
     *
     * @param sql SQL Statement
     * @param cfg Customize your prepare statement
     * @return Rows affected
     * <pre>
     *     executeUpdate("UPDATE foo SET bar = ? WHERE id = ?", (ps) -> {
     *         ps.setString(1, "foobar");
     *         ps.setInt(2, 41);
     *     });
     * </pre>
     */
    public int executeUpdate(String sql, ThrowingConsumer<PreparedStatement> cfg) {
        return wrapException(sql, s -> {
            cfg.accept(s);
            return s.executeUpdate();
        });
    }

    /**
     * Execute an SQL query with only result set
     *
     * @param sql SQL Statement
     * @param op  Result set
     * @param <R> Return values
     * @return the result set
     * <pre>
     * String value = executeQuery("SELECT STRING_COL FROM SOME_TABLE WHERE ID=42",
     *                             rs -> rs.next()? rs.getString(1): null);
     * int max = executeQuery("SELECT MAX(INTVAL_COL) FROM SOME_TABLE",
     *                        rs -> rs.next()? rs.getInt(1): -1);
     * </pre>
     */
    public <R> R executeQuery(String sql, ThrowingFunction<ResultSet, R, SQLException> op) {
        return wrapException(sql, s -> op.apply(s.executeQuery(sql)));
    }

    /**
     * Executes an SQL update
     *
     * @param sql SQL statement
     * @return rows affected
     * <pre>
     *     executeUpdate("UPDATE foo SET life = 60 WHERE bar_count > 50");
     * </pre>
     */
    public int executeUpdate(String sql) {
        return wrapException(sql, s -> s.executeUpdate(sql));
    }

    private <T> T wrapException(String sql, ThrowingFunction<PreparedStatement, T, SQLException> operation) {
        try(Connection connection = getConnectionType().getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                return operation.apply(statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> extends Consumer<T> {

        @Override
        default void accept(final T elem) {
            try {
                acceptThrows(elem);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        void acceptThrows(T elem) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T input) throws E;
    }
}
