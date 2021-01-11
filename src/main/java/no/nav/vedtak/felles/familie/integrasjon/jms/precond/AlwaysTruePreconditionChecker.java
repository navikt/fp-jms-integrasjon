package no.nav.vedtak.felles.familie.integrasjon.jms.precond;

public class AlwaysTruePreconditionChecker implements PreconditionChecker {

    @Override
    public PreconditionCheckerResult check() {
        return PreconditionCheckerResult.fullfilled();
    }
}
