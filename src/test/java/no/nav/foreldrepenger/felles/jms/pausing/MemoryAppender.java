package no.nav.foreldrepenger.felles.jms.pausing;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class MemoryAppender extends ListAppender<ILoggingEvent> {
    MemoryAppender(String name) {
        this.name = name;
    }

    void reset() {
        this.list.clear();
    }

    List<ILoggingEvent> search(String string, Level level) {
        return this.list.stream().filter(event -> event.getMessage().contains(string) && event.getLevel().equals(level)).collect(Collectors.toList());
    }

    static MemoryAppender sniff(Class<?> clazz) {
        return sniff(LoggerFactory.getLogger(clazz));
    }

    static MemoryAppender sniff(org.slf4j.Logger logger) {
        var log = (Logger) logger;
        log.setLevel(Level.INFO);
        var sniffer = new MemoryAppender(log.getName());
        log.addAppender(sniffer);
        sniffer.start();
        return sniffer;
    }
}
