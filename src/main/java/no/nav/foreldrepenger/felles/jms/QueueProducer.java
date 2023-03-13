package no.nav.foreldrepenger.felles.jms;

import jakarta.jms.JMSException;

/**
 * Baseklasse for sending av meldinger til køer.
 */
public abstract class QueueProducer extends QueueProducerBase {

    protected QueueProducer() {
    }

    protected QueueProducer(JmsKonfig konfig) {
        super(konfig);
    }

    protected QueueProducer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        //NOOP ikke selftest for eksterne køer
    }
}
