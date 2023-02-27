package no.nav.vedtak.felles.integrasjon.jms.pausing;

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
        backoffHandlerExceptionOnCreateContext = new LinearBackoffHandler(TimeUnit.SECONDS.toMillis(5), 5);
        backoffHandlerUnfulfilledPrecondition = new LinearBackoffHandler(TimeUnit.SECONDS.toMillis(5), 5);
        backoffHandlerExceptionOnReceive = new LinearBackoffHandler(TimeUnit.SECONDS.toMillis(5), 5);
        backoffHandlerExceptionOnHandle = new LinearBackoffHandler(TimeUnit.SECONDS.toMillis(2), 3);
    }

    public void resetStateForAll() {
        resetStateForExceptionOnCreateContext();
        resetStateForUnfulfilledPrecondition();
        resetStateForExceptionOnReceive();
        resetStateForExceptionOnHandle();
    }

    public void resetStateForExceptionOnCreateContext() {
        backoffHandlerExceptionOnCreateContext.reset();
    }

    public void handleExceptionOnCreateContext(Exception e) {
        var mqErrorText = MQExceptionUtil.extract(e);
        logger.error("F-158357 Klarte ikke å connecte til MQ server: " + mqErrorText, e);
        var pauseLength = backoffHandlerExceptionOnCreateContext.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }

    public void handleUnfulfilledPrecondition(String errorMessage) {
        logger.error("F-310549 Precondition ikke oppfyllt: {}", errorMessage);
        var pauseLength = backoffHandlerUnfulfilledPrecondition.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }


    public void resetStateForUnfulfilledPrecondition() {
        backoffHandlerUnfulfilledPrecondition.reset();
    }

    public void handleExceptionOnReceive(Exception e) {
        var mqErrorText = MQExceptionUtil.extract(e);
        logger.error("F-266229 Uventet feil ved mottak av melding: {}", mqErrorText, e);
        var pauseLength = backoffHandlerExceptionOnReceive.getNextPauseLengthInMillisecs();
        pauseMillisecs(pauseLength);
    }

    public void resetStateForExceptionOnReceive() {
        backoffHandlerExceptionOnReceive.reset();
    }

    public void handleExceptionOnHandle(Exception e) {
        var mqErrorText = MQExceptionUtil.extract(e);
        logger.error("F-848912 Uventet feil ved håndtering av melding: {}", mqErrorText, e);
        var pauseLength = backoffHandlerExceptionOnHandle.getNextPauseLengthInMillisecs();
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
}
