package no.nav.vedtak.felles.integrasjon.dokdist;

import no.nav.ukelonn.integrasjon.Kode;


public enum AdresseType implements Kode {
    NORSK("norskPostadresse"),
    UTENLANDSK("utenlandskPostadresse");

    private String kode;

    AdresseType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
