package no.nav.journalpostapi.dto.sak;

import no.nav.ukelonn.integrasjon.Kode;

public enum Arkivsaksystem implements Kode {
    GSAK,
    PSAK;

    @Override
    public String getKode() {
        return name();
    }
}
