package no.nav.vedtak.felles.integrasjon.jms;

import java.util.Enumeration;

import javax.jms.JMSException;

/**
 * Baseklasse for meldingsdrevne beans for "interne" køer (dvs. fysisk på samme MQ server som VL bruker).
 */
public abstract class InternalQueueConsumer extends QueueConsumer {

    public InternalQueueConsumer() {
    }

    public InternalQueueConsumer(JmsKonfig konfig) {
        super(konfig);
    }

    public InternalQueueConsumer(JmsKonfig konfig, int sessionMode) {
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
