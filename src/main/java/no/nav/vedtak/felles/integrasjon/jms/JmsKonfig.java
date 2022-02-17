package no.nav.vedtak.felles.integrasjon.jms;

/**
 * Gir konfigurasjonsverdier felles for alle meldingskøer brukt i VL.
 * I praksis går disse verdiene på forbindelsen til selve MQ-serveren som VL bruker.
 * </p>
 * <p>
 * De enkelte meldingskøene har sin konkrete sub-klasse, med konfigurasjonsverdier for selve køen.
 */
public record JmsKonfig(String queueManagerHost,
                 int queueManagerPort,
                 String queueManagerName,
                 String queueManagerChannelName,
                 String queueManagerUsername,
                 String queueManagerPassword,
                 String queueName,
                 String replyToQueueName) {

    public boolean harReplyToQueue() {
        return replyToQueueName() != null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + ", queue=" + queueManagerName()
            + ", channel=" + queueManagerChannelName()
            + ", host=" + queueManagerHost()
            + ", port" + queueManagerPort()
            + ", username=" + queueManagerUsername()
            + ">";
    }
}
