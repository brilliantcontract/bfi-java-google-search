package bc.bfi.google_search;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Serper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Serper.class);
    private static final String SERPER_URL = "https://google.serper.dev/search";
    private static final int MAX_PAGES = 50;

    private final Gson gson;
    private final String apiKey;

    public Serper(final String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "Serper API key must not be null.");
        this.gson = new Gson();
        Unirest.config().connectTimeout(60_000);
    }

    public List<ResultItem> search(final String query) {
        Objects.requireNonNull(query, "Query must not be null.");
        final List<ResultItem> items = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            final String body = buildRequestBody(query, page);
            final HttpResponse<String> response = executeRequest(body);
            if (response == null) {
                LOGGER.warn("No response returned for page {}.", Integer.valueOf(page));
                break;
            }
            if (response.getStatus() != 200) {
                LOGGER.error("Failed to fetch page {} from Serper. HTTP status: {}", Integer.valueOf(page),
                        Integer.valueOf(response.getStatus()));
                break;
            }

            final SerperResponse serperResponse = parseResponse(response.getBody());
            if (serperResponse == null) {
                LOGGER.warn("Empty Serper response for page {}.", Integer.valueOf(page));
                break;
            }

            final List<OrganicResult> organic = serperResponse.getOrganic();
            if (organic == null || organic.isEmpty()) {
                LOGGER.info("No more organic results for query '{}' after page {}.", query,
                        Integer.valueOf(page));
                break;
            }

            for (OrganicResult result : organic) {
                if (result == null) {
                    continue;
                }
                final String title = result.getTitle() != null ? result.getTitle() : "";
                final String snippet = result.getSnippet() != null ? result.getSnippet() : "";
                final String link = result.getLink() != null ? result.getLink() : "";
                final int position = result.getPosition() != null ? result.getPosition().intValue() : 0;
                final ResultItem item = new ResultItem(snippet, link, page, position, query, title);
                items.add(item);
            }
        }

        return items;
    }

    private HttpResponse<String> executeRequest(final String body) {
        Objects.requireNonNull(body, "Request body must not be null.");
        try {
            return Unirest.post(SERPER_URL)
                    .header("X-API-KEY", apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asString();
        } catch (UnirestException exception) {
            LOGGER.error("Failed to execute request to Serper.", exception);
            return null;
        }
    }

    private String buildRequestBody(final String query, final int page) {
        assert page > 0 : "Page must be positive. Got: " + page;
        final RequestPayload payload = new RequestPayload();
        payload.setQuery(query);
        payload.setPage(page);
        return gson.toJson(payload);
    }

    private SerperResponse parseResponse(final String responseBody) {
        Objects.requireNonNull(responseBody, "Response body must not be null.");
        return gson.fromJson(responseBody, SerperResponse.class);
    }

    private static final class RequestPayload {

        @SerializedName("q")
        private String query;

        @SerializedName("page")
        private Integer page;

        void setQuery(final String queryValue) {
            this.query = queryValue;
        }

        void setPage(final int pageValue) {
            assert pageValue > 0 : "Page must be positive. Got: " + pageValue;
            this.page = Integer.valueOf(pageValue);
        }
    }

    private static final class SerperResponse {

        @SerializedName("organic")
        private List<OrganicResult> organic;

        List<OrganicResult> getOrganic() {
            return organic;
        }
    }

    private static final class OrganicResult {

        @SerializedName("snippet")
        private String snippet;

        @SerializedName("link")
        private String link;

        @SerializedName("position")
        private Integer position;

        @SerializedName("title")
        private String title;

        String getSnippet() {
            return snippet;
        }

        String getLink() {
            return link;
        }

        Integer getPosition() {
            return position;
        }

        String getTitle() {
            return title;
        }
    }
}
