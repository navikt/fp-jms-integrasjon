package no.nav.foreldrepenger.felles.jms;

public interface MdcHandler {
    void setCallId(String callId);

    void settNyCallId();

    void removeCallId();
}
