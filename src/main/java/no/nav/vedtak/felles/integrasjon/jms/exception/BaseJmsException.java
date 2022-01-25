package no.nav.vedtak.felles.integrasjon.jms.exception;

public abstract class BaseJmsException extends RuntimeException {

    public BaseJmsException(String message, Object[] parametre) {
        super(LoggerUtils.removeLineBreaks(String.format(message, parametre)));
    }

    public BaseJmsException(String message, Throwable cause, Object[] parametre) {
        super(LoggerUtils.removeLineBreaks(String.format(message, parametre)), cause);
    }
}
