package no.nav.vedtak.felles.integrasjon.dokdist;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokdistKlient {

    private OidcRestClient oidcRestClient;
    private URI dokdistUri;

    DokdistKlient() {
        //for CDI proxy
    }

    @Inject
    public DokdistKlient(OidcRestClient oidcRestClient, @KonfigVerdi(value = "dokdist.rest.distribuer.journalpost") String dokdistUrl) {
        this.oidcRestClient = oidcRestClient;
        this.dokdistUri = UriBuilder.fromUri(dokdistUrl).build();
    }

    public DistribuerJournalpostResponse distribuerJournalpost(DistribuerJournalpostRequest request) {
        return oidcRestClient.post(dokdistUri, request, DistribuerJournalpostResponse.class);
    }

}
