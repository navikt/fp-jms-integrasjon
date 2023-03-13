package no.nav.foreldrepenger.felles.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.Connection;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@SuppressWarnings("resource")
class SelftestQueueProducerBaseTest {
    private static final String MSG_TEXT = "beskjeden";
    private QueueProducer helper; // the object we're testing
    private JMSContext mockContext;
    private Queue mockQueue;
    private JMSProducer mockProducer;
    private TextMessage mockMessage;

    @BeforeEach
    void setup() throws JMSException {

        mockContext = mock(JMSContext.class);
        mockQueue = mock(Queue.class);
        var mockConsumer = mock(JMSConsumer.class);
        mockProducer = mock(JMSProducer.class);
        mockMessage = mock(TextMessage.class);
        var mockBrowser = mock(QueueBrowser.class);

        var jmsKonfig = new JmsKonfig("host", 0, "manager", "channel", "user", "pass", "queue", null);
        helper = new TestProducer(jmsKonfig) {
            @Override
            protected JMSContext createContext() {
                return mockContext;
            }
        };

        when(mockContext.createConsumer(mockQueue)).thenReturn(mockConsumer);
        when(mockContext.createProducer()).thenReturn(mockProducer);
        when(mockContext.createBrowser(mockQueue)).thenReturn(mockBrowser);
        var connection = mock(Connection.class);
        var session = mock(Session.class);
        var messageProducer = mock(MessageProducer.class);
        when(session.createProducer(any(Queue.class))).thenReturn(messageProducer);
        when(connection.createSession()).thenReturn(session);

        when(mockMessage.getText()).thenReturn(MSG_TEXT);

        helper.setQueue(mockQueue);
    }

    @Test
    void test_sendMessage() {

        helper.sendMessage(mockMessage);

        verify(mockProducer).send(mockQueue, mockMessage);
    }

    @Test
    void test_sendTextMessage() {
        final var build = JmsMessage.builder().withMessage(MSG_TEXT).build();
        helper.sendTextMessage(build);

        verify(mockProducer).send(mockQueue, MSG_TEXT);
    }

    @Test
    void test_sendTextMessageWithProperties() {

        final var build = JmsMessage.builder().withMessage(MSG_TEXT).addHeader("myKey1", "myValue1").addHeader("myKey2", "myValue2").build();
        helper.sendTextMessage(build);

        verify(mockProducer).setProperty("myKey1", "myValue1");
        verify(mockProducer).setProperty("myKey2", "myValue2");
        verify(mockProducer).send(mockQueue, MSG_TEXT);
    }

    @Test
    void test_sendTextMessageWithNullProperties() {
        final var build = JmsMessage.builder().withMessage(MSG_TEXT).build();
        helper.sendTextMessage(build);

        verify(mockProducer, never()).setProperty(anyString(), anyString());
        verify(mockProducer).send(mockQueue, MSG_TEXT);
    }

    @Test
    void testconnection() throws JMSException {
        helper.testConnection();
    }

    private static class TestProducer extends QueueProducer {

        TestProducer(JmsKonfig konfig) {
            super(konfig);
        }

        @Override
        public boolean isDisabled() {
            return false;
        }
    }
}
