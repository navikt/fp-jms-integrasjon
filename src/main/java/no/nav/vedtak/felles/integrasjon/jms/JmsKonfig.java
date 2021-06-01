package no.nav.vedtak.felles.integrasjon.jms;

import no.nav.vedtak.felles.integrasjon.jms.exception.KritiskJmsException;

/**
 * Definerer konfig for en JMS meldingskø.
 * Bruke {@link Named} for å identifisere en gitt queue konfigurasjon.
 */
public interface JmsKonfig {

    String getQueueManagerChannelName();

    String getQueueManagerHostname();

    String getQueueManagerName();

    int getQueueManagerPort();

    default String getQueueManagerUsername() {
        throw new KritiskJmsException("F-620269 Required method getQueueManagerUsername not implemented.");
    }

    default String getQueueManagerPassword() {
        throw new KritiskJmsException("F-620270 Required method getQueueManagerPassword not implemented.");
    }

    String getQueueName();

    boolean harReplyToQueue();

    String getReplyToQueueName();



}
