package no.nav.foreldrepenger.felles.jms.sessionmode;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.JMSContext;

@SuppressWarnings("resource")
class ClientAckSessionModeStrategyTest {

    private ClientAckSessionModeStrategy strategy; // the object we're testing

    private JMSContext mockJMSContext;

    @BeforeEach
    void setup() {
        strategy = new ClientAckSessionModeStrategy();
        mockJMSContext = mock(JMSContext.class);
    }

    @Test
    void test_getSessionMode() {
        var sessionMode = strategy.getSessionMode();
        assertThat(sessionMode).isEqualTo(JMSContext.CLIENT_ACKNOWLEDGE);
    }

    @Test
    void test_commitReceivedMessage() {
        strategy.commitReceivedMessage(mockJMSContext);

        verify(mockJMSContext).acknowledge();
    }

    @Test
    void test_rollbackReceivedMessage() {
        strategy.rollbackReceivedMessage(mockJMSContext, null, null);

        verify(mockJMSContext).recover();
    }
}
