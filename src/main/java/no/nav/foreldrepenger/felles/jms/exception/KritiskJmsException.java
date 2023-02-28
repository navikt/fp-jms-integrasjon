package no.nav.foreldrepenger.felles.jms.exception;

public class KritiskJmsException extends BaseJmsException {

    public KritiskJmsException(String message, Throwable cause, Object... parametre) {
        super(message, cause, parametre);
    }
}
