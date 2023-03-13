package no.nav.foreldrepenger.felles.jms.exception;

public abstract class BaseJmsException extends RuntimeException {

    protected BaseJmsException(String message, Throwable cause, Object[] parametre) {
        super(LoggerUtils.removeLineBreaks(String.format(message, parametre)), cause);
    }
}
