package no.nav.vedtak.felles.integrasjon.dokdist.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import no.nav.vedtak.felles.integrasjon.dokdist.Kode;

public class KodelisteSomKodeSerialiserer extends JsonSerializer {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof Kode) {
            Kode verdi = (Kode) o;
            jsonGenerator.writeString(verdi.getKode());
        } else {
            throw new IllegalArgumentException("Serialiserer kan bare brukes for Kode");
        }
    }
}