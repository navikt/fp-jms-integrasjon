package no.nav.foreldrepenger.felles.jms.pausing;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;

class DefaultErrorHandlingStrategyTest {

    private DefaultErrorHandlingStrategy strategy; // the object we're testing

    private static MemoryAppender logSniffer;

    @BeforeEach
    void setUp() {
        logSniffer = MemoryAppender.sniff(DefaultErrorHandlingStrategy.class);
        strategy = new DefaultErrorHandlingStrategy();
    }

    @AfterEach
    void afterEach() {
        logSniffer.reset();
    }

    @Test
    void test_handleExceptionOnCreateContext() {
        Exception e = new RuntimeException("oida");
        doAndAssertPause(() -> strategy.handleExceptionOnCreateContext(e));
        Assertions.assertThat(logSniffer.search("F-158357", Level.ERROR)).hasSize(1);
    }

    @Test
    void test_handleUnfulfilledPrecondition() {
        doAndAssertPause(() -> strategy.handleUnfulfilledPrecondition("test_handleUnfulfilledPrecondition"));
        Assertions.assertThat(logSniffer.search("F-310549", Level.ERROR)).hasSize(1);
    }

    @Test
    void test_handleExceptionOnReceive() {
        Exception e = new RuntimeException("uffda");
        doAndAssertPause(() -> strategy.handleExceptionOnReceive(e));
        Assertions.assertThat(logSniffer.search("F-266229", Level.ERROR)).hasSize(1);
    }

    @Test
    void test_handleExceptionOnHandle() {
        Exception e = new RuntimeException("auda");
        doAndAssertPause(() -> strategy.handleExceptionOnHandle(e));
        Assertions.assertThat(logSniffer.search("F-848912", Level.ERROR)).hasSize(1);
    }

    private void doAndAssertPause(Runnable pausingAction) {
        var before = System.currentTimeMillis();
        pausingAction.run();
        var after = System.currentTimeMillis();
        var actualPause = after - before;
        assertThat(actualPause).isGreaterThanOrEqualTo(2000L);
    }
}
