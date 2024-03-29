package no.nav.foreldrepenger.felles.jms;

import java.util.function.Consumer;

import com.ibm.mq.jakarta.jms.MQQueue;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import no.nav.foreldrepenger.felles.jms.exception.KritiskJmsException;
import no.nav.foreldrepenger.felles.jms.pausing.MQExceptionUtil;

/**
 * Baseklasse for klienter som skriver meldinger til kø.
 */
public abstract class QueueProducerBase extends QueueBase {

    protected QueueProducerBase() {
    }

    protected QueueProducerBase(JmsKonfig konfig) {
        super(konfig);
    }

    protected QueueProducerBase(JmsKonfig konfig, int sessionMode) {
        super(konfig, sessionMode);
    }

    protected JMSContext createContext() {
        return getConnectionFactory().createContext(getUsername(), getPassword(), getSessionMode());
    }

    protected void doWithContext(Consumer<JMSContext> consumer) {
        if (isDisabled()) {
            return;
        }

        // TODO (MS) : JMSContext kan caches per tråd i en ThreadLocal for å redusere turnover av ressurser hvis det
        // blir et ytelsesproblem. Bør antagelig da lages nytt ved Exception.
        try (var context = createContext()) {
            consumer.accept(context);
        } catch (JMSRuntimeException e) {
            var mqExceptionDetails = MQExceptionUtil.extract(e);
            throw new KritiskJmsException("F-848912 Uventet feil ved håndtering av melding: %s", e, mqExceptionDetails);
        }
    }

    public void sendMessage(Message message) {
        doWithContext(ctx -> doSendMessage(message, ctx));
    }

    protected void doSendMessage(Message message, JMSContext context) {
        if (isDisabled()) {
            return;
        }

        var producer = context.createProducer();
        producer.send(getQueue(), message);
    }

    public void sendTextMessage(JmsMessage message) {
        doWithContext(ctx -> doSendTextMessage(message, ctx));
    }

    protected void doSendTextMessage(JmsMessage message, JMSContext context) {
        if (isDisabled()) {
            return;
        }

        var producer = context.createProducer();
        if (getKonfig().harReplyToQueue()) {
            registrerReplyToQueue(producer);
        }
        if (message.hasHeaders()) {
            for (var entry : message.getHeaders().entrySet()) {
                producer.setProperty(entry.getKey(), entry.getValue());
            }
        }
        producer.send(getQueue(), message.getText());
    }

    private void registrerReplyToQueue(JMSProducer producer) {
        var konfig = getKonfig();
        try {
            var replyToQueue = new MQQueue(konfig.queueManagerName(), konfig.replyToQueueName());
            producer.setJMSReplyTo(replyToQueue);
        } catch (JMSException e) {
            throw new KritiskJmsException("JMSException ved oppsett av replyTo", e);
        }
    }
}
