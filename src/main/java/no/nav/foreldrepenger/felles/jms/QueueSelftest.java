package no.nav.foreldrepenger.felles.jms;

import jakarta.jms.JMSException;

/**
 * Metoder for å understøtte selftest av en meldingskø.
 *
 * @see <a href="https://confluence.adeo.no/display/AURA/Selftest">AURA Selftest</a>
 */
public interface QueueSelftest {

    void testConnection() throws JMSException;

    String getConnectionEndpoint();
}
