package bc.bfi.google_search;

import java.sql.Connection;
import java.sql.SQLException;

interface DatabaseConnector {

    Connection connect(final String jdbcUrl, final String username, final String password) throws SQLException;
}
