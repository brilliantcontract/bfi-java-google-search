package bc.bfi.google_search;

public class Config {
    
    /**
     * Online services API keys.
     */
    final static String SCRAPENINJA_API_KEY = "455e2a6556msheffc310f7420b51p102ea0jsn1c531be1e299";
    final static String SERPER_API_KEY = "0881769d8ef5e996ba41abc92ddb186b00d1a9b1";

    /**
     * Database.
     */
    static final int QUERY_LIMIT = 1000;
    static final String DB_HOST = "3.140.167.34";
    static final String DB_PORT = "5432";
    static final String DB_USERNAME = "redash";
    static final String DB_PASSWORD = "te83NECug38ueP";
    static final String DB_DATABASE = "scrapers";
    static final String DB_TABLE_SEARCHES = "google_search.searches";
    static final String DB_TABLE_QUERIES_TO_SCRAPE = "google_search.queries_to_scrape_vw";
}
