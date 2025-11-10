package bc.bfi.google_search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryRepository.class);
    private static final String COLUMN_QUERY = "query";
    private static final String JDBC_PREFIX = "jdbc:postgresql://";

    private final String jdbcUrl;

    public QueryRepository() {
        this.jdbcUrl = JDBC_PREFIX + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_DATABASE;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("PostgreSQL JDBC driver is not available.", exception);
        }
    }

    public List<String> fetchQueries(final int limit) {
        assert limit > 0 : "Limit must be greater than zero. Got: " + limit;
        final List<String> queries = new ArrayList<>();
        final String sql = "SELECT " + COLUMN_QUERY + " FROM " + Config.DB_TABLE_QUERIES_TO_SCRAPE + " LIMIT ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, Config.DB_USERNAME, Config.DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final String query = resultSet.getString(COLUMN_QUERY);
                    if (query == null || query.isEmpty()) {
                        continue;
                    }
                    queries.add(query);
                }
            }
        } catch (SQLException exception) {
            LOGGER.error("Failed to fetch queries from database.", exception);
            throw new IllegalStateException("Unable to load queries from database.", exception);
        }

        return queries;
    }
}
