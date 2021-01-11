package no.nav.vedtak.felles.familie.integrasjon.jms;

public interface MdcHandler {
    void setCallId(String callId);

    void settNyCallId();

    void removeCallId();
}
