package no.nav.journalpostapi.dto.dokument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

import java.util.Base64;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dokumentvariant {
    private String filnavn;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Filtype filtype;
    private byte[] fysiskDokument;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Variantformat variantformat;

    public Variantformat getVariantformat() {
        return variantformat;
    }

    private Dokumentvariant() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFilnavn() {
        return filnavn;
    }

    public Filtype getFiltype() {
        return filtype;
    }

    public byte[] getFysiskDokument() {
        return fysiskDokument;
    }

    public static class Builder {

        private Dokumentvariant kladd = new Dokumentvariant();

        public Builder medDokument(byte[] dokumentInnhold) {
            kladd.fysiskDokument = dokumentInnhold;
            return this;
        }

        public Builder medFiltype(Filtype filtype) {
            kladd.filtype = filtype;
            return this;
        }

        public Builder medVariantformat(Variantformat variantformat) {
            kladd.variantformat = variantformat;
            return this;
        }

        public Builder medFilnavn(String filnavn) {
            kladd.filnavn = filnavn;
            return this;
        }

        public Dokumentvariant build() {
            Objects.requireNonNull(kladd.fysiskDokument, "mangler dokumentinnhold");
            Objects.requireNonNull(kladd.filtype, "mangler filtype");
            Objects.requireNonNull(kladd.variantformat, "mangler variantformat");
            return kladd;
        }
    }
}