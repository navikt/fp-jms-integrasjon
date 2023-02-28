package no.nav.foreldrepenger.felles.jms.pausing;

class NoBackoffHandler extends ErrorHandler {

    public NoBackoffHandler() {
        super(0, 0);
    }

    public NoBackoffHandler(long initialWait) {
        super(initialWait, 0);
    }

    @Override
    public long getNextPauseLengthInMillisecs() {
        return getInitialWait();
    }

}
