package no.nav.vedtak.felles.integrasjon.jms;

import jakarta.jms.JMSException;

/**
 * Baseklasse for meldingsdrevne beans for køer.
 */
public abstract class ExternalQueueConsumer extends QueueConsumerBase {

    public ExternalQueueConsumer() {
    }

    public ExternalQueueConsumer(JmsKonfig konfig) {
        super(konfig);
    }

    public ExternalQueueConsumer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        if (isDisabled()) return;

        try (var context = createContext()) {
            try (var consumer = context.createConsumer(getQueue());) {
                // autocloses
            }
        }
    }
}
