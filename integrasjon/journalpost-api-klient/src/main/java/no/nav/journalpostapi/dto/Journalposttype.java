package no.nav.journalpostapi.dto;

public enum Journalposttype implements Kode {
    INNGÅENDE("INNGAAENDE"),
    UTGÅENDE("UTGAAENDE"),
    NOTAT("NOTAT");

    private String kode;

    Journalposttype(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
