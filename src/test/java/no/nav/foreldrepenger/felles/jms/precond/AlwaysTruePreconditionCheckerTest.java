package no.nav.foreldrepenger.felles.jms.precond;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;

class AlwaysTruePreconditionCheckerTest {

    @Test
    void test_isFulfilled() {
        var checker = new AlwaysTruePreconditionChecker();

        var checkerResult = checker.check();
        assertThat(checkerResult.isFulfilled()).isTrue();
        assertThat(checkerResult.getErrorMessage().isPresent()).isFalse();
    }
}
