package no.nav.vedtak.felles.integrasjon.jms.precond;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("resource")
public class DefaultDatabaseOppePreconditionCheckerTest {

    private DefaultDatabaseOppePreconditionChecker preconditionChecker; // objektet vi tester

    private DataSource mockDataSource;

    @BeforeEach
    public void setup() throws SQLException {

        mockDataSource = mock(DataSource.class);

        preconditionChecker = new DefaultDatabaseOppePreconditionChecker();
        preconditionChecker.setDataSource(mockDataSource);
    }

    @Test
    public void test_isFulfilled_dbOppe() throws SQLException {
        var mockConnection = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        var checkerResult = preconditionChecker.check();
        assertThat(checkerResult.isFulfilled()).isTrue();
        assertThat(checkerResult.getErrorMessage().isPresent()).isFalse();
    }

    @Test
    public void test_isFulfilled_dbNede() throws SQLException {
        when(mockDataSource.getConnection()).thenThrow(new SQLException("ikke-tom-feilmelding"));

        var checkerResult = preconditionChecker.check();
        assertThat(checkerResult.isFulfilled()).isFalse();
        assertThat(checkerResult.getErrorMessage().isPresent()).isTrue();
    }

}
