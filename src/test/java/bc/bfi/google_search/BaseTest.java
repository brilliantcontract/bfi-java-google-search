package bc.bfi.google_search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public final class BaseTest {

    @Test
    public void saveSearchesInsertsAllResults() throws SQLException {
        // Initialization.

        final ResultItem firstItem = new ResultItem("Description one", "https://example.com/1", 1, 1,
                "test query", "Title One");
        final ResultItem secondItem = new ResultItem("Description two", "https://example.com/2", 2, 3,
                "test query", "Title Two");
        final List<ResultItem> results = new ArrayList<>();
        results.add(firstItem);
        results.add(secondItem);

        final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(statement);
        final RecordingDatabaseConnector connector = new RecordingDatabaseConnector(connection);
        final Base base = new Base(connector);

        // Execution.

        base.saveSearches(results);

        // Assertion.

        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(connection).prepareStatement(sqlCaptor.capture());
        final String expectedSql = "INSERT INTO " + Config.DB_TABLE_SEARCHES
                + " (description, link, page_number, position, query, title) VALUES (?, ?, ?, ?, ?, ?)";
        MatcherAssert.assertThat(sqlCaptor.getValue(), Matchers.is(expectedSql));

        final ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(statement, Mockito.times(2)).setString(Mockito.eq(1), descriptionCaptor.capture());
        MatcherAssert.assertThat(descriptionCaptor.getAllValues().get(0), Matchers.is(firstItem.getDescription()));
        MatcherAssert.assertThat(descriptionCaptor.getAllValues().get(1), Matchers.is(secondItem.getDescription()));

        final ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(statement, Mockito.times(2)).setString(Mockito.eq(2), linkCaptor.capture());
        MatcherAssert.assertThat(linkCaptor.getAllValues().get(0), Matchers.is(firstItem.getLink()));
        MatcherAssert.assertThat(linkCaptor.getAllValues().get(1), Matchers.is(secondItem.getLink()));

        final ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(statement, Mockito.times(2)).setInt(Mockito.eq(3), pageCaptor.capture());
        MatcherAssert.assertThat(pageCaptor.getAllValues().get(0), Matchers.is(Integer.valueOf(firstItem.getPageNumber())));
        MatcherAssert.assertThat(pageCaptor.getAllValues().get(1), Matchers.is(Integer.valueOf(secondItem.getPageNumber())));

        final ArgumentCaptor<Integer> positionCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(statement, Mockito.times(2)).setInt(Mockito.eq(4), positionCaptor.capture());
        MatcherAssert.assertThat(positionCaptor.getAllValues().get(0),
                Matchers.is(Integer.valueOf(firstItem.getPosition())));
        MatcherAssert.assertThat(positionCaptor.getAllValues().get(1),
                Matchers.is(Integer.valueOf(secondItem.getPosition())));

        final ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(statement, Mockito.times(2)).setString(Mockito.eq(5), queryCaptor.capture());
        MatcherAssert.assertThat(queryCaptor.getAllValues().get(0), Matchers.is(firstItem.getQuery()));
        MatcherAssert.assertThat(queryCaptor.getAllValues().get(1), Matchers.is(secondItem.getQuery()));

        final ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(statement, Mockito.times(2)).setString(Mockito.eq(6), titleCaptor.capture());
        MatcherAssert.assertThat(titleCaptor.getAllValues().get(0), Matchers.is(firstItem.getTitle()));
        MatcherAssert.assertThat(titleCaptor.getAllValues().get(1), Matchers.is(secondItem.getTitle()));

        Mockito.verify(statement, Mockito.times(2)).addBatch();
        Mockito.verify(statement).executeBatch();
        MatcherAssert.assertThat(Integer.valueOf(connector.getInvocationCount()), Matchers.is(Integer.valueOf(1)));
    }

    @Test
    public void saveSearchesSkipsWhenListIsEmpty() throws SQLException {
        // Initialization.

        final List<ResultItem> results = new ArrayList<>();
        final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        final Connection connection = Mockito.mock(Connection.class);
        final RecordingDatabaseConnector connector = new RecordingDatabaseConnector(connection);
        final Base base = new Base(connector);

        // Execution.

        base.saveSearches(results);

        // Assertion.

        MatcherAssert.assertThat(Integer.valueOf(connector.getInvocationCount()), Matchers.is(Integer.valueOf(0)));
        Mockito.verifyNoInteractions(connection);
        Mockito.verifyNoInteractions(statement);
    }

    @Test
    public void saveSearchesThrowsExceptionWhenListIsNull() {
        // Initialization.

        final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        final Connection connection = Mockito.mock(Connection.class);
        final RecordingDatabaseConnector connector = new RecordingDatabaseConnector(connection);
        final Base base = new Base(connector);

        // Execution.

        final Executable call = new Executable() {
            @Override
            public void execute() {
                base.saveSearches(null);
            }
        };

        // Assertion.

        final NullPointerException exception = org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, call);
        MatcherAssert.assertThat(exception.getMessage(), Matchers.is("Search results must not be null."));
    }

    private static final class RecordingDatabaseConnector implements DatabaseConnector {

        private final Connection connection;
        private int invocationCount;

        RecordingDatabaseConnector(final Connection connectionValue) {
            this.connection = connectionValue;
            this.invocationCount = 0;
        }

        @Override
        public Connection connect(final String jdbcUrl, final String username, final String password) {
            invocationCount++;
            return connection;
        }

        int getInvocationCount() {
            return invocationCount;
        }
    }
}
