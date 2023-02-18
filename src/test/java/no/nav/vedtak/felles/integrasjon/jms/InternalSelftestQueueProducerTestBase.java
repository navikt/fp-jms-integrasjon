package no.nav.vedtak.felles.integrasjon.jms;

import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@SuppressWarnings("resource")
public class InternalSelftestQueueProducerTestBase {

    private ExternalQueueProducer helper; // the object we're testing

    private JMSContext mockContext;
    private Queue mockQueue;
    private JMSProducer mockProducer;
    private QueueBrowser mockBrowser;
    private TextMessage mockMessage;

    private static final String MSG_TEXT = "beskjeden";

    @BeforeEach
    public void setup() throws JMSException {

        mockContext = mock(JMSContext.class);
        mockQueue = mock(Queue.class);
        var mockConsumer = mock(JMSConsumer.class);
        mockProducer = mock(JMSProducer.class);
        mockMessage = mock(TextMessage.class);
        mockBrowser = mock(QueueBrowser.class);
        var jmsKonfig = new JmsKonfig("host",
            0,
            "manager",
            "channel",
            "user",
            "pass",
            "queue",
            null);

        helper = new TestInternalQueueProducer(jmsKonfig) {
            @Override
            protected JMSContext createContext() {
                return mockContext;
            }
        };
        when(mockContext.createConsumer(mockQueue)).thenReturn(mockConsumer);
        when(mockContext.createProducer()).thenReturn(mockProducer);
        when(mockContext.createBrowser(mockQueue)).thenReturn(mockBrowser);

        when(mockMessage.getText()).thenReturn(MSG_TEXT);

        helper.setQueue(mockQueue);
    }

    @Test
    public void test_sendMessage() {

        helper.sendMessage(mockMessage);

        verify(mockProducer).send(mockQueue, mockMessage);
    }

    @Test
    public void test_sendTextMessage() {
        final var build = JmsMessage.builder().withMessage(MSG_TEXT).build();
        helper.sendTextMessage(build);

        verify(mockProducer).send(mockQueue, MSG_TEXT);
    }

    private static class TestInternalQueueProducer extends ExternalQueueProducer {

        TestInternalQueueProducer(JmsKonfig konfig) {
            super(konfig);
        }

        @Override
        public boolean isDisabled() {
            return false;
        }
    }
}
