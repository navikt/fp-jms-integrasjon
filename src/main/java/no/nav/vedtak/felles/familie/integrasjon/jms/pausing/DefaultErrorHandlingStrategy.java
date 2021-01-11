package no.nav.vedtak.felles.familie.integrasjon.jms.pausing;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultErrorHandlingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorHandlingStrategy.class);

    private ErrorHandler backoffHandlerExceptionOnCreateContext;
    private ErrorHandler backoffHandlerUnfulfilledPrecondition;
    private ErrorHandler backoffHandlerExceptionOnReceive;
    private ErrorHandler backoffHandlerExceptionOnHandle;

    // Feil i handle() vil typisk være pga feil i selve meldingen eller i applikasjonens data,
    // så bruke kortere venting.

    public DefaultErrorHandlingStrategy() {
        backoffHandlerExceptionOnCreateContext = new LinearBackoffHandler(sekunderTilMillisekunder(5), 5);
        backoffHandlerUnfulfilledPrecondition = new LinearBackoffHandler(sekunderTilMillisekunder(5), 5);
        backoffHandlerExceptionOnReceive = new LinearBackoffHandler(sekunderTilMillisekunder(5), 5);
        backoffHandlerExceptionOnHandle = new LinearBackoffHandler(sekunderTilMillisekunder(2), 3);
    }

    public void resetStateForAll() {
        resetStateForExceptionOnCreateContext();
        resetStateForUnfulfilledPrecondition();
        resetStateForExceptionOnReceive();
        resetStateForExceptionOnHandle();
    }


    public void handleExceptionOnCreateContext(Exception e) {
        CharSequence mqErrorText = MQExceptionUtil.extract(e);
        logger.error("F-158357 Klarte ikke å connecte til MQ server: " + mqErrorText, e);
        long pauseLength = backoffHandlerExceptionOnCreateContext.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }

    public void resetStateForExceptionOnCreateContext() {
        backoffHandlerExceptionOnCreateContext.reset();
    }

    public void handleUnfulfilledPrecondition(String errorMessage) {
        logger.error("F-310549 Precondition ikke oppfyllt: {}", errorMessage);
        long pauseLength = backoffHandlerUnfulfilledPrecondition.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }


    public void resetStateForUnfulfilledPrecondition() {
        backoffHandlerUnfulfilledPrecondition.reset();
    }

    public void handleExceptionOnReceive(Exception e) {
        CharSequence mqErrorText = MQExceptionUtil.extract(e);
        logger.error("F-266229 Uventet feil ved mottak av melding: " + mqErrorText, e);
        long pauseLength = backoffHandlerExceptionOnReceive.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }

    public void resetStateForExceptionOnReceive() {
        backoffHandlerExceptionOnReceive.reset();
    }

    public void handleExceptionOnHandle(Exception e) {
        CharSequence mqErrorText = MQExceptionUtil.extract(e);
        logger.error("F-848912 Uventet feil ved håndtering av melding: " + mqErrorText, e);
        long pauseLength = backoffHandlerExceptionOnHandle.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }

    public void resetStateForExceptionOnHandle() {
        backoffHandlerExceptionOnHandle.reset();
    }

    protected void pauseMillisecs(long millisecs) {
        logger.debug("Pause {} millisecs", millisecs);
        try {
            TimeUnit.MILLISECONDS.sleep(millisecs);
        } catch (InterruptedException e) { //NOSONAR Ikke thread shutdown som pågår her
            logger.error("F-551390 Pausing avbrutt");
        }
    }

    protected final long sekunderTilMillisekunder(int secs) {
        return secs * 1000L;
    }
}
