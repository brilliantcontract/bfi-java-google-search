package bc.bfi.google_search;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final int QUERY_LIMIT = 1000;

    public static void main(final String[] args) {
        final Base repository = new Base();
        final List<String> queries = repository.fetchQueries(QUERY_LIMIT);
        LOGGER.info("Loaded {} queries from database.", Integer.valueOf(queries.size()));

        if (queries.isEmpty()) {
            LOGGER.warn("No queries were returned from the database.");
            return;
        }

        for (String query : queries) {
            final Serper serper = new Serper(Config.SERPER_API_KEY);
            LOGGER.info("Executing Serper search for the first query: {}", query);
            final List<ResultItem> results = serper.search(query);
            LOGGER.info("Serper returned {} results for query '{}'.", Integer.valueOf(results.size()), query);
        }
    }
}
