package no.nav.foreldrepenger.felles.jms;

import jakarta.jms.JMSException;

/**
 * Baseklasse for meldingsdrevne beans for køer.
 */
public abstract class QueueConsumer extends QueueConsumerBase {

    protected QueueConsumer() {
    }

    protected QueueConsumer(JmsKonfig konfig) {
        super(konfig);
    }

    protected QueueConsumer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        if (isDisabled()) {
            return;
        }

        try (var context = createContext()) {
            try (var consumer = context.createConsumer(getQueue());) {
                // autocloses
            }
        }
    }
}
