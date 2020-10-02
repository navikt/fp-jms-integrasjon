package no.nav.vedtak.felles.integrasjon.dokument.produksjon.v3;

import static no.nav.vedtak.sts.client.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.dokumentproduksjon.v3.DokumentproduksjonV3;
import no.nav.vedtak.sts.client.StsClientType;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class DokumentproduksjonConsumerProducer {
    private DokumentproduksjonConsumerConfig consumerConfig;

    @Inject
    public void setConfig(DokumentproduksjonConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public DokumentproduksjonConsumer dokumentproduksjonConsumer() {
        DokumentproduksjonV3 port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new DokumentproduksjonConsumerImpl(port);
    }

    public DokumentproduksjonSelftestConsumer dokumentproduksjonSelftestConsumer() {
        DokumentproduksjonV3 port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new DokumentproduksjonSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    DokumentproduksjonV3 wrapWithSts(DokumentproduksjonV3 port, StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}