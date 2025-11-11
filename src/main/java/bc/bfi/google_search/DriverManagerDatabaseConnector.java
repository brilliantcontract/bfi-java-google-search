package bc.bfi.google_search;

import java.util.Objects;
import org.sql2o.Sql2o;

final class DriverManagerDatabaseConnector implements DatabaseConnector {

    @Override
    public Sql2o connect(final String jdbcUrl, final String username, final String password) {
        Objects.requireNonNull(jdbcUrl, "JDBC URL must not be null.");
        Objects.requireNonNull(username, "Database username must not be null.");
        Objects.requireNonNull(password, "Database password must not be null.");
        return new Sql2o(jdbcUrl, username, password);
    }
}
