package bc.bfi.google_search;

import java.util.Objects;

public final class ResultItem {

    private final String description;
    private final String link;
    private final int pageNumber;
    private final int position;
    private final String query;
    private final String title;

    public ResultItem(final String description, final String link, final int pageNumber,
            final int position, final String query, final String title) {
        this.description = description != null ? description : "";
        this.link = link != null ? link : "";
        this.pageNumber = pageNumber;
        this.position = position;
        this.query = Objects.requireNonNull(query, "Query must not be null.");
        this.title = title != null ? title : "";
        assert this.pageNumber > 0 : "Page number must be positive. Got: " + this.pageNumber;
        assert this.position >= 0 : "Result position must be zero or positive. Got: " + this.position;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPosition() {
        return position;
    }

    public String getQuery() {
        return query;
    }

    public String getTitle() {
        return title;
    }
}
