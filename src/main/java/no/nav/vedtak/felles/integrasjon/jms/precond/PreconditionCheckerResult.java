package no.nav.vedtak.felles.integrasjon.jms.precond;

import java.util.Optional;

public final class PreconditionCheckerResult {

    private final boolean isFulfilled;
    private final String errorMessage;

    private PreconditionCheckerResult(boolean isFulfilled, String errorMessage) {
        this.isFulfilled = isFulfilled;
        this.errorMessage = errorMessage;
    }

    public static PreconditionCheckerResult fullfilled() {
        return new PreconditionCheckerResult(true, null);
    }

    public static PreconditionCheckerResult notFullfilled(String errorMessage) {
        return new PreconditionCheckerResult(false, errorMessage);
    }

    public boolean isFulfilled() {
        return isFulfilled;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
