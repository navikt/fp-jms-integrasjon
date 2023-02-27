package no.nav.vedtak.felles.integrasjon.jms;

import jakarta.jms.JMSException;

/**
 * Baseklasse for sending av meldinger til køer.
 */
public abstract class QueueProducer extends QueueProducerBase {

    public QueueProducer() {
    }

    public QueueProducer(JmsKonfig konfig) {
        super(konfig);
    }

    public QueueProducer(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    @Override
    public void testConnection() throws JMSException {
        //NOOP ikke selftest for eksterne køer
    }
}
