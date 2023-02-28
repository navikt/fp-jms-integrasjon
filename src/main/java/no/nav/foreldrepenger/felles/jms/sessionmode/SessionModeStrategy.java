package no.nav.foreldrepenger.felles.jms.sessionmode;

import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Queue;

/**
 * Strategi for commit / rollback av meldinger man lykkes / ikke lykkes å prosessere.
 */
public interface SessionModeStrategy {

    int getSessionMode();

    void commitReceivedMessage(JMSContext context);

    void rollbackReceivedMessage(JMSContext context, Queue queue, Message message);
}
