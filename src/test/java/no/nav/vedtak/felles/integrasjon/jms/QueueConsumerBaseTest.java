package no.nav.vedtak.felles.integrasjon.jms;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.TextMessage;
import no.nav.vedtak.felles.integrasjon.jms.pausing.DefaultErrorHandlingStrategy;
import no.nav.vedtak.felles.integrasjon.jms.precond.PreconditionChecker;
import no.nav.vedtak.felles.integrasjon.jms.precond.PreconditionCheckerResult;
import no.nav.vedtak.felles.integrasjon.jms.sessionmode.SessionModeStrategy;

@SuppressWarnings("resource")
public class QueueConsumerBaseTest {

    private static final int SLEEP = 200;

    private QueueConsumerBase asyncJmsConsumer; // the object we're testing

    private JMSContext mockJMSContext;
    private Queue mockQueue;
    private JMSConsumer mockJMSConsumer;
    private QueueBrowser mockQueueBrowser;
    private SessionModeStrategy mockSessionModeStrategy;
    private DefaultErrorHandlingStrategy mockErrorHandlingStrategy;
    private PreconditionChecker mockPreconditionChecker;
    private TextMessage mockMessage;

    private JmsKonfig konfig;

    @BeforeEach
    public void setup() throws JMSException {
        konfig = new JmsKonfig("host", 0, "manager", "channel", "user", "pass", "queue", null);
        mockJMSContext = mock(JMSContext.class);
        mockQueue = mock(Queue.class);
        mockJMSConsumer = mock(JMSConsumer.class);
        mockQueueBrowser = mock(QueueBrowser.class);
        mockSessionModeStrategy = mock(SessionModeStrategy.class);
        mockErrorHandlingStrategy = mock(DefaultErrorHandlingStrategy.class);
        mockPreconditionChecker = mock(PreconditionChecker.class);
        mockMessage = mock(TextMessage.class);

        asyncJmsConsumer = new InternalTestQueueConsumer(konfig) {
            @Override
            protected JMSContext createContext() {
                return mockJMSContext;
            }

            @Override
            public boolean isDisabled() {
                return false;
            }
        };

        when(mockJMSContext.createConsumer(any(Queue.class))).thenReturn(mockJMSConsumer);
        when(mockJMSContext.createBrowser(any(Queue.class))).thenReturn(mockQueueBrowser);

        when(mockPreconditionChecker.check()).thenReturn(PreconditionCheckerResult.fullfilled());

        Answer<Void> pauseAnswer = invocation -> {
            TimeUnit.MILLISECONDS.sleep(2);
            return null;
        };
        doAnswer(pauseAnswer).when(mockErrorHandlingStrategy).handleExceptionOnCreateContext(any(Exception.class));
        doAnswer(pauseAnswer).when(mockErrorHandlingStrategy).handleUnfulfilledPrecondition(any(String.class));
        doAnswer(pauseAnswer).when(mockErrorHandlingStrategy).handleExceptionOnReceive(any(Exception.class));

        asyncJmsConsumer.setQueue(mockQueue);
    }

    @Test
    public void test_start_stop() throws InterruptedException {

        // timeout
        doAnswer((Answer<Void>) invocation -> {
            TimeUnit.MILLISECONDS.sleep(50);
            return null; // tom kø
        }).when(mockJMSConsumer).receive(anyLong());

        asyncJmsConsumer.start();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();
        verify(mockErrorHandlingStrategy).resetStateForAll();

        TimeUnit.MILLISECONDS.sleep(SLEEP);
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();

        asyncJmsConsumer.stop();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isFalse();

        verify(mockJMSContext).close();
    }

    @Test
    public void test_receiveLoop_typical() throws InterruptedException, JMSException {

        final var initialMsgsOnQueue = 3;
        mockMessagesOnQueue(initialMsgsOnQueue);

        asyncJmsConsumer.start();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();

        // la loopen gå litt, så vi får konsumert meldinger
        TimeUnit.MILLISECONDS.sleep(SLEEP);

        verify(mockPreconditionChecker, atLeast(initialMsgsOnQueue)).check();
        verify(mockJMSConsumer, atLeast(initialMsgsOnQueue)).receive(anyLong());
        verify(mockSessionModeStrategy, times(initialMsgsOnQueue)).commitReceivedMessage(any(JMSContext.class));
        verify(mockSessionModeStrategy, never()).rollbackReceivedMessage(any(), any(), any());
        verify(mockErrorHandlingStrategy, atLeast(initialMsgsOnQueue)).resetStateForUnfulfilledPrecondition();
        verify(mockErrorHandlingStrategy, atLeast(initialMsgsOnQueue)).resetStateForExceptionOnReceive();
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnCreateContext(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).handleUnfulfilledPrecondition(any(String.class));
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnReceive(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnHandle(any(Exception.class));
        verify(mockJMSContext, never()).close();

        asyncJmsConsumer.stop();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isFalse();

        verify(mockJMSContext).close();
    }

    @Test
    public void test_receiveLoop_exceptionWhenCreatingContext() throws InterruptedException, JMSException {

        asyncJmsConsumer = new InternalTestQueueConsumer(konfig) {
            @Override
            protected JMSContext createContext() {
                throw new JMSRuntimeException("!!");
            }
        };

        asyncJmsConsumer.start();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();

        // la loopen gå litt
        TimeUnit.MILLISECONDS.sleep(SLEEP);

        verify(mockPreconditionChecker, never()).check();
        verify(mockJMSConsumer, never()).receive(anyLong());
        verify(mockSessionModeStrategy, never()).commitReceivedMessage(any());
        verify(mockSessionModeStrategy, never()).rollbackReceivedMessage(any(), any(), any());
        verify(mockErrorHandlingStrategy, atLeast(2)).handleExceptionOnCreateContext(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).handleUnfulfilledPrecondition(any(String.class));
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnReceive(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnCreateContext();
        verify(mockErrorHandlingStrategy, never()).resetStateForUnfulfilledPrecondition();
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnReceive();
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnHandle();

        asyncJmsConsumer.stop();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isFalse();

        verify(mockJMSContext, never()).close();
    }

    @Test
    public void test_receiveLoop_precondNotFulfilled() throws InterruptedException, JMSException {

        final var initialMsgsOnQueue = 2; // men vi skal aldri faktisk lese dem, pga false precond
        when(mockPreconditionChecker.check()).thenReturn(PreconditionCheckerResult.notFullfilled("Feilmelding"));

        asyncJmsConsumer.start();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();

        // la loopen gå litt
        TimeUnit.MILLISECONDS.sleep(SLEEP);

        verify(mockPreconditionChecker, atLeast(initialMsgsOnQueue)).check();
        verify(mockJMSConsumer, never()).receive(anyLong());
        verify(mockSessionModeStrategy, never()).commitReceivedMessage(any());
        verify(mockSessionModeStrategy, never()).rollbackReceivedMessage(any(), any(), any());
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnCreateContext(any(Exception.class));
        verify(mockErrorHandlingStrategy, atLeast(initialMsgsOnQueue)).handleUnfulfilledPrecondition(any(String.class));
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnReceive(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).resetStateForUnfulfilledPrecondition();
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnReceive();
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnHandle();
        verify(mockJMSContext, never()).close();

        asyncJmsConsumer.stop();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isFalse();

        verify(mockJMSContext).close();
    }

    @Test
    public void test_receiveLoop_exceptionWhenReceivingMessage() throws InterruptedException, JMSException {

        when(mockJMSConsumer.receive(anyLong())).thenThrow(new JMSRuntimeException("!!!!"));

        asyncJmsConsumer.start();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();

        // la loopen gå litt
        TimeUnit.MILLISECONDS.sleep(SLEEP);

        verify(mockPreconditionChecker, atLeast(1)).check();
        verify(mockJMSConsumer, atLeast(1)).receive(anyLong());
        verify(mockSessionModeStrategy, never()).commitReceivedMessage(any());
        verify(mockSessionModeStrategy, never()).rollbackReceivedMessage(any(), any(), any());
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnCreateContext(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).handleUnfulfilledPrecondition(any(String.class));
        verify(mockErrorHandlingStrategy, atLeast(1)).handleExceptionOnReceive(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnReceive();
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnHandle();
        verify(mockJMSContext, atLeast(1)).close();

        asyncJmsConsumer.stop();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isFalse();

        verify(mockJMSContext, atLeast(2)).close();
    }

    @Test
    public void test_receiveLoop_exceptionInHandler() throws InterruptedException, JMSException {

        final var initialMsgsOnQueue = 99999999; // slik at vi alltid kaller messageHandler.handle()
        mockMessagesOnQueue(initialMsgsOnQueue);
        asyncJmsConsumer = new FailingQueueConsumer(konfig) {
            @Override
            protected JMSContext createContext() {
                return mockJMSContext;
            }
        };
        asyncJmsConsumer.start();
        assertThat(asyncJmsConsumer.isStartedAndRunning()).isTrue();

        // la loopen gå litt
        TimeUnit.MILLISECONDS.sleep(SLEEP);

        verify(mockPreconditionChecker, atLeast(1)).check();
        verify(mockJMSConsumer, atLeast(1)).receive(anyLong());
        verify(mockSessionModeStrategy, never()).commitReceivedMessage(any(JMSContext.class));
        verify(mockSessionModeStrategy, atLeast(1)).rollbackReceivedMessage(any(), any(), any());
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnReceive(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).handleExceptionOnCreateContext(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).handleUnfulfilledPrecondition(any(String.class));
        verify(mockErrorHandlingStrategy, atLeast(1)).handleExceptionOnHandle(any(Exception.class));
        verify(mockErrorHandlingStrategy, never()).resetStateForExceptionOnHandle();
        verify(mockJMSContext, never()).close();

        asyncJmsConsumer.stop();

        assertThat(asyncJmsConsumer.isStartedAndRunning()).isFalse();
        verify(mockJMSContext).close();
    }

    @Test
    public void test_testConnection_queueIsOnOtherQueueMgr() throws JMSException {

        asyncJmsConsumer = new TestQueueConsumer(konfig) {
            @Override
            protected JMSContext createContext() {
                return mockJMSContext;
            }
        };
        var jmsProducer = mock(JMSProducer.class);
        when(mockJMSContext.createProducer()).thenReturn(jmsProducer);

        asyncJmsConsumer.testConnection();

        verify(mockJMSContext, never()).createBrowser(any(Queue.class));
        verify(mockJMSContext).close();
    }

    @Test
    public void test_getConnectionEndpoint() throws JMSException {

        var host = "someHost";
        var port = 9097;
        var someMgr = "someMgr";
        var queue = "someQueue";
        JmsKonfig eksternKonfig = new JmsKonfig(host, port, someMgr, null, "someUsername", "myInjectedPassword", queue, null);

        var testingAsyncJmsConsumer = new InternalTestQueueConsumer(eksternKonfig);
        var endpoint = testingAsyncJmsConsumer.getConnectionEndpoint();

        assertThat(endpoint).contains(someMgr);
        assertThat(endpoint).contains(host);
        assertThat(endpoint).contains(String.valueOf(port));
        assertThat(endpoint).contains(queue);
    }

    // ----------------------

    private void mockMessagesOnQueue(int numMsgs) {

        doAnswer(new Answer<TextMessage>() {
            private int remainingMsgsOnQueue = numMsgs;

            @Override
            public TextMessage answer(InvocationOnMock invocation) throws Throwable {
                if (remainingMsgsOnQueue >= 1) {
                    remainingMsgsOnQueue -= 1;
                    return mockMessage;
                } else {
                    TimeUnit.MILLISECONDS.sleep(1);
                    return null;
                }
            }
        }).when(mockJMSConsumer).receive(anyLong());
    }

    class InternalTestQueueConsumer extends QueueConsumer {

        public InternalTestQueueConsumer(JmsKonfig konfig) {
            super(konfig);
            this.setQueue(mockQueue);
        }

        @Override
        public SessionModeStrategy getSessionModeStrategy() {
            return mockSessionModeStrategy;
        }

        @Override
        public DefaultErrorHandlingStrategy getErrorHandlingStrategy() {
            return mockErrorHandlingStrategy;
        }

        @Override
        public void handle(Message message) throws JMSException {
            ((TextMessage) message).getText();
        }

        @Override
        public PreconditionChecker getPreconditionChecker() {
            return mockPreconditionChecker;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }
    }

    class TestQueueConsumer extends QueueConsumer {

        public TestQueueConsumer(JmsKonfig konfig) {
            super(konfig);
            this.setQueue(mockQueue);
        }

        @Override
        public SessionModeStrategy getSessionModeStrategy() {
            return mockSessionModeStrategy;
        }

        @Override
        public DefaultErrorHandlingStrategy getErrorHandlingStrategy() {
            return mockErrorHandlingStrategy;
        }

        @Override
        public void handle(Message message) throws JMSException {
            ((TextMessage) message).getText();
        }

        @Override
        public PreconditionChecker getPreconditionChecker() {
            return mockPreconditionChecker;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

    }

    class FailingQueueConsumer extends QueueConsumer {

        public FailingQueueConsumer(JmsKonfig konfig) {
            super(konfig);
            this.setQueue(mockQueue);
        }

        @Override
        public SessionModeStrategy getSessionModeStrategy() {
            return mockSessionModeStrategy;
        }

        @Override
        public DefaultErrorHandlingStrategy getErrorHandlingStrategy() {
            return mockErrorHandlingStrategy;
        }

        @Override
        public void handle(Message message) throws JMSException {
            throw new JMSException("");
        }

        @Override
        public PreconditionChecker getPreconditionChecker() {
            return mockPreconditionChecker;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

    }
}
