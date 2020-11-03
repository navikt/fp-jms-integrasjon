package no.nav.journalpostapi;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.journalpostapi.dto.opprett.OpprettJournalpostRequest;
import no.nav.journalpostapi.dto.opprett.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class JournalpostApiKlient {

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    JournalpostApiKlient() {
        //for CDI proxy
    }

    @Inject
    public JournalpostApiKlient(OidcRestClient oidcRestClient, @KonfigVerdi(value = "journalpost.opprett", defaultVerdi = "http://dokarkiv/rest/journalpostapi/v1/journalpost") URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        return opprettJournalpost(request, true);
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean forsøkFerdigstill) {
        URI uri = UriBuilder.fromUri(endpoint)
            .queryParam("forsoekFerdigstill", forsøkFerdigstill)
            .build();
        return oidcRestClient.post(uri, request, OpprettJournalpostResponse.class);
    }
}
