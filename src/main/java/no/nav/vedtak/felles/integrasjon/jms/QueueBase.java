package no.nav.vedtak.felles.integrasjon.jms;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

/**
 * Felleskode for å bruke meldingskøer, uavhengig om køen leses eller skrives.
 */
abstract class QueueBase implements QueueSelftest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ToggleJms toggleJms;

    private final JmsKonfig konfig;
    private int sessionMode = JMSContext.CLIENT_ACKNOWLEDGE;
    private ConnectionFactory connectionFactory;
    private Queue queue;

    public QueueBase() {
        // CDI
        konfig = null;
    }

    public QueueBase(JmsKonfig konfig) {
        Objects.requireNonNull(konfig, "konfig");
        this.konfig = konfig;
    }

    public QueueBase(JmsKonfig konfig, int sessionMode) {
        Objects.requireNonNull(konfig, "konfig");
        this.konfig = konfig;
        this.sessionMode = sessionMode;
    }

    protected void setToggleJms(ToggleJms toggleJms) {
        this.toggleJms = toggleJms;
    }

    @Override
    public String getConnectionEndpoint() {
        return String.format("%s@%s (%s:%d)", // NOSONAR
            getKonfig().queueName(), getKonfig().queueManagerName(), getKonfig().queueManagerHost(), getKonfig().queueManagerPort());
    }

    protected void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected void setQueue(Queue queue) {
        this.queue = queue;
    }

    public JmsKonfig getKonfig() {
        return konfig;
    }

    public String getUsername() {
        return konfig.queueManagerUsername();
    }

    public int getSessionMode() {
        return sessionMode;
    }

    protected String getPassword() {
        return konfig.queueManagerPassword();
    }

    protected ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    protected Queue getQueue() {
        return queue;
    }

    public boolean isDisabled() {
        return toggleJms != null && toggleJms.isDisabled();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "konfig=" + konfig + ", sessionMode=" + sessionMode + ">";
    }

}
