package no.nav.vedtak.felles.integrasjon.jms;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Brukes til å starte/stoppe meldingsdrevne beans.
 */
@ApplicationScoped
public class QueueConsumerManagerImpl implements QueueConsumerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueConsumerManagerImpl.class);

    private List<QueueConsumer> consumerList;

    private ToggleJms toggleJms;

    // Får inn (indirekte) liste over alle beans av type QueueConsumer
    @Inject
    public void initConsumers(@Any Instance<QueueConsumer> consumersInstance, Instance<ToggleJms> toggleJms, Instance<MdcHandler> mdcHandlers) { // NOSONAR Joda, kalles av CDI
        if (toggleJms == null || toggleJms.isUnsatisfied()) {
            LOGGER.info("Ingen ToggleJms er tilgjengelig, JMS blir dermed værende på");
        } else {
            this.toggleJms = toggleJms.get();
        }
        if (isDisabled()) return;

        MdcHandler mdcHandler = null;
        if (mdcHandlers == null || mdcHandlers.isUnsatisfied()) {
            LOGGER.warn("Ingen MdcHandler er tilgjengelig, MDC kontext blir ikke satt riktig");
        } else {
            mdcHandler = mdcHandlers.get();
        }

        consumerList = new ArrayList<>();
        for (var consumer : consumersInstance) {
            consumer.setToggleJms(this.toggleJms);
            consumer.setMdcHandler(mdcHandler);
            consumerList.add(consumer);

        }
        LOGGER.info("initConsumers la til {} consumers", consumerList.size());
    }


    public boolean isDisabled() {
        return toggleJms.isDisabled();
    }

    @Override
    public synchronized void start() {
        if (isDisabled()) return;

        LOGGER.debug("start ...");
        for (var consumer : consumerList) {
            consumer.start();
        }
        LOGGER.info("startet");
    }

    @Override
    public synchronized void stop() {
        if (isDisabled()) return;

        LOGGER.debug("stop ...");
        for (var consumer : consumerList) {
            consumer.stop();
        }
        LOGGER.info("stoppet");
    }
}
