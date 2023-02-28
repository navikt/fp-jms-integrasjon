package no.nav.foreldrepenger.felles.jms.precond;

public class AlwaysTruePreconditionChecker implements PreconditionChecker {

    @Override
    public PreconditionCheckerResult check() {
        return PreconditionCheckerResult.fullfilled();
    }
}
