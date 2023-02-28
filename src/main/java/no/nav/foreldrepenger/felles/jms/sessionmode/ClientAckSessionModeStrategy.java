package no.nav.foreldrepenger.felles.jms.sessionmode;

import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Queue;

public class ClientAckSessionModeStrategy implements SessionModeStrategy {

    @Override
    public int getSessionMode() {
        return JMSContext.CLIENT_ACKNOWLEDGE;
    }

    @Override
    public void commitReceivedMessage(JMSContext context) {
        context.acknowledge();
    }

    @Override
    public void rollbackReceivedMessage(JMSContext context, Queue queue, Message message) {
        context.recover();
    }
}
