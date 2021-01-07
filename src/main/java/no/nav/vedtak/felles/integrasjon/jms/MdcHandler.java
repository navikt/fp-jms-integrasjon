package no.nav.vedtak.felles.integrasjon.jms;

public interface MdcHandler {
    void setCallId(String callId);

    void settNyCallId();

    void removeCallId();
}
