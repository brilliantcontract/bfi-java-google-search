package bc.bfi.google_search;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

public final class Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);
    private static final String COLUMN_QUERY = "query";
    private static final String JDBC_PREFIX = "jdbc:postgresql://";
    private static final String SELECT_QUERIES_SQL = "SELECT " + COLUMN_QUERY + " FROM "
            + Config.DB_TABLE_QUERIES_TO_SCRAPE + " LIMIT :limit";
    private static final String INSERT_SEARCH_SQL = "INSERT INTO " + Config.DB_TABLE_SEARCHES
            + " (description, link, page_number, position, query, title) VALUES (:description, :link, :pageNumber, :position,"
            + " :query, :title)";

    private final Sql2o sql2o;

    public Base() {
        this(createDefaultSql2o());
    }

    Base(final Sql2o sql2o) {
        this.sql2o = Objects.requireNonNull(sql2o, "Sql2o instance must not be null.");
    }

    private static Sql2o createDefaultSql2o() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("PostgreSQL JDBC driver is not available.", exception);
        }

        final String jdbcUrl = JDBC_PREFIX + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_DATABASE;
        return createSql2o(jdbcUrl, Config.DB_USERNAME, Config.DB_PASSWORD);
    }

    private static Sql2o createSql2o(final String jdbcUrl, final String username, final String password) {
        Objects.requireNonNull(jdbcUrl, "JDBC URL must not be null.");
        Objects.requireNonNull(username, "Database username must not be null.");
        Objects.requireNonNull(password, "Database password must not be null.");
        return new Sql2o(jdbcUrl, username, password);
    }

    public List<String> fetchQueries(final int limit) {
        assert limit > 0 : "Limit must be greater than zero. Got: " + limit;
        final List<String> queries = new ArrayList<>();
        try (Connection connection = sql2o.open()) {
            final Query statement = connection.createQuery(SELECT_QUERIES_SQL);
            statement.addParameter("limit", limit);
            final List<String> rawQueries = statement.executeScalarList(String.class);
            for (String query : rawQueries) {
                if (query == null || query.isEmpty()) {
                    continue;
                }
                queries.add(query);
            }
        } catch (Sql2oException exception) {
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

        try (Connection connection = sql2o.beginTransaction()) {
            for (SearchResultItem item : searchResults) {
                if (item == null) {
                    LOGGER.warn("Encountered null search result while saving. Skipping entry.");
                    continue;
                }
                final Query insertQuery = connection.createQuery(INSERT_SEARCH_SQL);
                insertQuery.addParameter("description", item.getDescription());
                insertQuery.addParameter("link", item.getLink());
                insertQuery.addParameter("pageNumber", item.getPageNumber());
                insertQuery.addParameter("position", item.getPosition());
                insertQuery.addParameter("query", item.getQuery());
                insertQuery.addParameter("title", item.getTitle());
                insertQuery.executeUpdate();
                savedCount++;
            }

            if (savedCount == 0) {
                LOGGER.info("No valid search results to save into database.");
                connection.rollback();
                return;
            }

            connection.commit();
        } catch (Sql2oException exception) {
            LOGGER.error("Failed to save search results into database.", exception);
            throw new IllegalStateException("Unable to save search results into database.", exception);
        }
    }
}
