package no.nav.vedtak.felles.familie.integrasjon.jms.exception;

public class KritiskJmsException extends BaseJmsException {

    public KritiskJmsException(String message, Object... parametre) {
        super(message, parametre);
    }

    public KritiskJmsException(String message, Throwable cause, Object... parametre) {
        super(message, cause, parametre);
    }
}
