package no.nav.vedtak.felles.integrasjon.jms;

import java.util.Enumeration;

import jakarta.jms.JMSException;

/**
 * Baseklasse for sending av meldinger til "interne" køer (dvs. fysisk på samme MQ server som VL bruker).
 */
@Deprecated(forRemoval = true, since = "2.1") // Bruk ExternalQueueProducer
public abstract class InternalQueueProducer extends QueueProducer {

    public InternalQueueProducer() {
    }

    public InternalQueueProducer(JmsKonfig konfig) {
        super(konfig);
    }

    public InternalQueueProducer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        if (isDisabled()) return;

        try (var context = createContext()) {
            try (var browser = context.createBrowser(getQueue())) {
                // Se på max. 1 melding, uten å konsumere den:
                Enumeration<?> msgsEnumeration = browser.getEnumeration();
                if (msgsEnumeration.hasMoreElements()) {
                    msgsEnumeration.nextElement();
                }
            }
        }
    }
}
