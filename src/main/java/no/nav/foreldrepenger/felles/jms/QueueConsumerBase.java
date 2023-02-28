package no.nav.foreldrepenger.felles.jms;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import no.nav.foreldrepenger.felles.jms.exception.LoggerUtils;
import no.nav.foreldrepenger.felles.jms.precond.PreconditionChecker;
import no.nav.foreldrepenger.felles.jms.sessionmode.ClientAckSessionModeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import no.nav.foreldrepenger.felles.jms.pausing.DefaultErrorHandlingStrategy;
import no.nav.foreldrepenger.felles.jms.precond.AlwaysTruePreconditionChecker;
import no.nav.foreldrepenger.felles.jms.sessionmode.SessionModeStrategy;

/**
 * Baseklasse for meldingsdrevne beans, dvs. beans som eksekverer på en egen tråd
 * og konsumerer meldinger fra en kø.
 *
 * @see <a href="https://confluence.adeo.no/pages/viewpage.action?pageId=218415758">Asynkron lesing fra meldingskø</a>
 */
abstract class QueueConsumerBase extends QueueBase {

    private static final long RECEIVE_TIMEOUT_MS = 300; // Ikke sett denne for lang - vil forsinke shutdown
    private static final long EXECUTOR_SHUTDOWN_TIMEOUT_MS = 10000;

    private ExecutorService executorService;
    private ReceiveLoop receiveLoop;

    private Semaphore startSemaphore = new Semaphore(0);

    private PeriodiskTimeoutVarsel timeoutVarsel = new PeriodiskTimeoutVarsel();

    private MdcHandler mdcHandler;

    public QueueConsumerBase() {
    }

    public QueueConsumerBase(JmsKonfig konfig) {
        super(konfig);
    }

    public QueueConsumerBase(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    protected void setMdcHandler(MdcHandler mdcHandler) {
        this.mdcHandler = mdcHandler;
    }

    protected JMSContext createContext() {
        return getConnectionFactory().createContext(getUsername(), getPassword(), getSessionMode());
    }

    // Default impl. Subclasses can override.
    public PreconditionChecker getPreconditionChecker() {
        return new AlwaysTruePreconditionChecker();
    }

    // Default impl. Subclasses can override.
    public SessionModeStrategy getSessionModeStrategy() {
        return new ClientAckSessionModeStrategy();
    }

    // Default impl. Subclasses can override.
    public DefaultErrorHandlingStrategy getErrorHandlingStrategy() {
        return new DefaultErrorHandlingStrategy();
    }

    public abstract void handle(Message message) throws JMSException;

    public Message receiveMessage(JMSContext context, Queue queue, long timeoutMillisecs) {
        Message message;
        try (var consumer = context.createConsumer(queue)) {
            message = consumer.receive(timeoutMillisecs);
        }

        timeoutVarsel.oppdater(message, timeoutMillisecs);
        return message;
    }

    public void start() {
        if (isDisabled()) {
            return;
        }

        if (executorService != null) {
            log.warn("receive loop allerede startet.");
            return;
        }
        getErrorHandlingStrategy().resetStateForAll();
        executorService = Executors.newSingleThreadExecutor(); // NOSONAR Vi vet at executorService er null her
        receiveLoop = new ReceiveLoop();
        executorService.execute(receiveLoop); // denne bare "bestiller" den nye tråden

        // For å gjøre testing enklere, vent på at den nye tråden faktisk har startet:
        waitUntilStartedAndRunning();
    }

    private void waitUntilStartedAndRunning() {
        var secs = 4;
        try {
            if (!startSemaphore.tryAcquire(secs, TimeUnit.SECONDS) || !receiveLoop.isRunning()) {
                log.warn("Klarte ikke å starte tråd etter {} s", secs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Klarte ikke å starte tråd etter {} s", secs);
        }
    }

    boolean isStartedAndRunning() {
        return (receiveLoop != null && receiveLoop.isRunning());
    }

    public void stop() {
        if (isDisabled()) {
            return;
        }

        if (executorService == null) {
            log.warn("receive loop ikke startet.");
            return;
        }
        receiveLoop.stopReceiveLoop();
        try {
            executorService.shutdown();
            executorService.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) { // NOSONAR
            log.warn("feil ved stopping av receive loop: awaitTermination avbrutt.");
        } finally {
            if (!executorService.isTerminated()) {
                log.warn("feil ved stopping av receive loop: executorService ikke terminert - gjør shutdownNow()");
                executorService.shutdownNow();
            }
            executorService = null;
            receiveLoop = null;
        }
    }

    /**
     * Implementerer loop for å hente ned og ack'e JMS meldinger.
     */
    class ReceiveLoop implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(ReceiveLoop.class);

        private final AtomicBoolean running = new AtomicBoolean();
        private final AtomicBoolean stopReceiveLoop = new AtomicBoolean();

        boolean isRunning() {
            return running.get();
        }

        void stopReceiveLoop() {
            stopReceiveLoop.set(true);
        }

        @Override
        public void run() {
            if (isDisabled()) {
                return;
            }

            if (logger.isInfoEnabled()) {
                logger.info("startet JMS: {}", LoggerUtils.toStringWithoutLineBreaks(getKonfig())); //$NON-NLS-1$ //NOSONAR
            }

            running.set(true);
            startSemaphore.release();

            while (!stopReceiveLoop.get()) {
                try (var context = createContext()) {
                    if (logger.isDebugEnabled()) {
                        log.debug("Laget ny context: {}", LoggerUtils.toStringWithoutLineBreaks(getKonfig())); //$NON-NLS-1$ //NOSONAR
                    }
                    getErrorHandlingStrategy().resetStateForExceptionOnCreateContext();
                    var shouldKeepContext = true;
                    while (shouldKeepContext && !stopReceiveLoop.get()) {
                        shouldKeepContext = receiveAndHandleOneMessageAndShallKeepContext(context);
                    }
                } catch (RuntimeException e) {
                    getErrorHandlingStrategy().handleExceptionOnCreateContext(e);
                    // ...men fortsett å loope
                }
            }
            running.set(false);
            startSemaphore.drainPermits();
            if (logger.isInfoEnabled()) {
                logger.info("avsluttet JMS: {}", LoggerUtils.toStringWithoutLineBreaks(getKonfig())); //$NON-NLS-1$
            }
        }

        boolean receiveAndHandleOneMessageAndShallKeepContext(JMSContext context) {

            var checkerResult = getPreconditionChecker().check();
            if (!checkerResult.isFulfilled()) {
                getErrorHandlingStrategy().handleUnfulfilledPrecondition(checkerResult.getErrorMessage().orElse("Ukjent feilmelding"));
                return true;
            }
            getErrorHandlingStrategy().resetStateForUnfulfilledPrecondition();

            Message message;
            try {
                message = receiveMessage(context, getQueue(), RECEIVE_TIMEOUT_MS);
            } catch (RuntimeException e) {
                getErrorHandlingStrategy().handleExceptionOnReceive(e);
                // Dette kan skje hvis connection er blitt stengt/ødelagt av andre, så signaliser at context skal
                // stenges:
                return false; // NOSONAR
            }
            getErrorHandlingStrategy().resetStateForExceptionOnReceive();

            if (message != null) {
                handleMessage(context, message);
            }
            return true;
        }

        void handleMessage(JMSContext context, Message message) {
            try {
                initCallId(message);

                logReceivingMessage(message);

                handle(message);
                getSessionModeStrategy().commitReceivedMessage(context);

                getErrorHandlingStrategy().resetStateForExceptionOnHandle(); // alt ok, så reset
            } catch (JMSException | RuntimeException e) {
                try {
                    getSessionModeStrategy().rollbackReceivedMessage(context, getQueue(), message);
                } catch (Exception e2) {
                    logger.warn("rollback feilet: {}", e2.getMessage(), e2);
                    // ...men bruk (ytre) e i backoff/logging nedenfor
                }
                getErrorHandlingStrategy().handleExceptionOnHandle(e);
                return;
            } finally {
                resetCallid();
            }
        }

        private void logReceivingMessage(Message message) throws JMSException {
            // tar ikke med callId her, kommer automatisk med i log format
            var messageId = message.getJMSMessageID();
            var correlationId = message.getJMSCorrelationID();
            log.info("Mottar melding. QueueName={}, Channel={}, MessageId={}, CorrelationId={}", getKonfig().queueName(),
                getKonfig().queueManagerChannelName(), messageId, correlationId);
        }

        void resetCallid() {
            if (mdcHandler != null) {
                mdcHandler.removeCallId(); // reset callId
            }
        }

        void initCallId(Message message) throws JMSException {
            if (mdcHandler != null) {
                var callId = message.getStringProperty("callId");
                if (callId != null) {
                    // set callId fra melding hvis eksisterer
                    mdcHandler.setCallId(callId);
                } else {
                    // lag ny
                    mdcHandler.settNyCallId();
                }
            }
        }

    }

    /**
     * Varsler periodisk timeout, istdf. kontinuerlig.
     */
    class PeriodiskTimeoutVarsel {
        private AtomicLong meldingCount = new AtomicLong();
        private AtomicLong timeoutCount = new AtomicLong();
        private LocalDateTime sistMottatt;

        private void timeout() {
            timeoutCount.incrementAndGet();
        }

        void oppdater(Message message, long timeoutMillis) {

            if (message == null) {
                timeout();
                // ikke logg mer enn 1 gang per minutt
                if (timeoutMillis > 0 && timeoutCount.incrementAndGet() % ((60 * 1000L) / timeoutMillis) == 0 && log.isDebugEnabled()) {
                    log.debug("Timeout - ingen melding mottatt siden {} på JMS queue: {}", sistMottatt, //$NON-NLS-1$
                        LoggerUtils.removeLineBreaks(getKonfig().queueName())); // NOSONAR
                }
            } else {
                mottattMelding();
            }
        }

        private void mottattMelding() {
            meldingCount.incrementAndGet();
            timeoutCount.set(0L);
            sistMottatt = LocalDateTime.now();
        }
    }

}
