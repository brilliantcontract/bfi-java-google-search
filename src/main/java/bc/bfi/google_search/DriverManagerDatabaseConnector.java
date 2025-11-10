package bc.bfi.google_search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

final class DriverManagerDatabaseConnector implements DatabaseConnector {

    @Override
    public Connection connect(final String jdbcUrl, final String username, final String password) throws SQLException {
        Objects.requireNonNull(jdbcUrl, "JDBC URL must not be null.");
        Objects.requireNonNull(username, "Database username must not be null.");
        Objects.requireNonNull(password, "Database password must not be null.");
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
