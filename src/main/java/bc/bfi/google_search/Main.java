package bc.bfi.google_search;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        final Base base = new Base();
        final Serper serper = new Serper(Config.SERPER_API_KEY);

        final List<String> queries = base.fetchQueries();
        LOGGER.info("Loaded {} queries from database.", Integer.valueOf(queries.size()));

        if (queries.isEmpty()) {
            LOGGER.warn("No queries were returned from the database.");
            return;
        }

        for (String query : queries) {
            LOGGER.info("Executing Serper search for the first query: {}", query);
            final List<SearchResultItem> searchResults = serper.search(query);
            LOGGER.info("Serper returned {} results for query '{}'.", Integer.valueOf(searchResults.size()), query);
            base.saveSearches(searchResults);
        }
    }
}
