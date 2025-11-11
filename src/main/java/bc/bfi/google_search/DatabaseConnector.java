package bc.bfi.google_search;

import org.sql2o.Sql2o;

interface DatabaseConnector {

    Sql2o connect(final String jdbcUrl, final String username, final String password);
}
