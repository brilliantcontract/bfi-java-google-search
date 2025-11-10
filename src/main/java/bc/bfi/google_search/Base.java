package bc.bfi.google_search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);
    private static final String COLUMN_QUERY = "query";
    private static final String JDBC_PREFIX = "jdbc:postgresql://";
    private static final String INSERT_SEARCH_SQL = "INSERT INTO " + Config.DB_TABLE_SEARCHES
            + " (description, link, page_number, position, query, title) VALUES (?, ?, ?, ?, ?, ?)";

    private final String jdbcUrl;
    private final DatabaseConnector connector;

    public Base() {
        this(new DriverManagerDatabaseConnector());
    }

    Base(final DatabaseConnector databaseConnector) {
        this.jdbcUrl = JDBC_PREFIX + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_DATABASE;
        this.connector = Objects.requireNonNull(databaseConnector, "Database connector must not be null.");
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

        try (Connection connection = connector.connect(jdbcUrl, Config.DB_USERNAME, Config.DB_PASSWORD);
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

    public void saveSearches(final List<SearchResultItem> searchResults) {
        Objects.requireNonNull(searchResults, "Search results must not be null.");
        if (searchResults.isEmpty()) {
            LOGGER.info("No search results to save into database.");
            return;
        }

        int savedCount = 0;

        try (Connection connection = connector.connect(jdbcUrl, Config.DB_USERNAME, Config.DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(INSERT_SEARCH_SQL)) {
            for (SearchResultItem item : searchResults) {
                if (item == null) {
                    LOGGER.warn("Encountered null search result while saving. Skipping entry.");
                    continue;
                }
                statement.setString(1, item.getDescription());
                statement.setString(2, item.getLink());
                statement.setInt(3, item.getPageNumber());
                statement.setInt(4, item.getPosition());
                statement.setString(5, item.getQuery());
                statement.setString(6, item.getTitle());
                statement.addBatch();
                savedCount++;
            }

            if (savedCount == 0) {
                LOGGER.info("No valid search results to save into database.");
                return;
            }

            statement.executeBatch();
        } catch (SQLException exception) {
            LOGGER.error("Failed to save search results into database.", exception);
            throw new IllegalStateException("Unable to save search results into database.", exception);
        }
    }
}
