package bc.bfi.google_search;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final int QUERY_LIMIT = 1000;

    public static void main(final String[] args) {
        final QueryRepository repository = new QueryRepository();
        final List<String> queries = repository.fetchQueries(QUERY_LIMIT);
        LOGGER.info("Loaded {} queries from database.", Integer.valueOf(queries.size()));

        if (queries.isEmpty()) {
            LOGGER.warn("No queries were returned from the database.");
            return;
        }

        final Serper serper = new Serper(Config.SERPER_API_KEY);
        final String firstQuery = queries.get(0);
        LOGGER.info("Executing Serper search for the first query: {}", firstQuery);
        final List<ResultItem> results = serper.search(firstQuery);
        LOGGER.info("Serper returned {} results for query '{}'.", Integer.valueOf(results.size()), firstQuery);
    }
}
