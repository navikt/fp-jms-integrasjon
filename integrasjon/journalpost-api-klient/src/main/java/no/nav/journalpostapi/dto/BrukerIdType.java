package no.nav.journalpostapi.dto;

public enum BrukerIdType implements Kode {
    NorskIdent("FNR"),
    Organisasjonsnummer("ORGNR"),
    AktørId("AKTOERID");

    private String kode;

    BrukerIdType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
