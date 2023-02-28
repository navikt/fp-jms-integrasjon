package no.nav.foreldrepenger.felles.jms.pausing;

class LinearBackoffHandler extends ErrorHandler {

    LinearBackoffHandler(long initialWait, int maxFailed) {
        super(initialWait, maxFailed);
    }

    @Override
    public long getNextPauseLengthInMillisecs() {
        var attempt = getFailedAttempts();
        incrementFailedAttempts();
        var wait = getInitialWait();
        wait += Math.multiplyExact(getInitialWait(), Long.min(getMaxFailed(), attempt));
        return wait;
    }
}
