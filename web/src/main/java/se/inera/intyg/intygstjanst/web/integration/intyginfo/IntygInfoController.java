/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.web.integration.intyginfo;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

/**
 * Internal REST endpoint for intyg oriented data.
 */
@Path("/intygInfo")
public class IntygInfoController {
    private static final Logger LOG = LoggerFactory.getLogger(IntygInfoController.class);

    @Autowired
    private CertificateService certificateService;
    @Autowired
    private RecipientService recipientService;
    @Autowired
    private IntygModuleRegistry moduleRegistry;


    @PrometheusTimeMethod
    @GET
    @Path("/certificate/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public IntygInfoResponse getCertificates(@PathParam("id") String id) {

        try {
            Certificate certificate = certificateService.getCertificateForCare(id);

            IntygInfoResponse response = new IntygInfoResponse();

            response.setIntygId(certificate.getId());
            response.setIntygType(certificate.getType());
            response.setIntygVersion(certificate.getTypeVersion());

            response.setSignedDate(certificate.getSignedDate());
            response.setReceivedDate(certificate.getOriginalCertificate().getReceived());

            List<CertificateStateHistoryEntry> states = certificate.getStates();
            Optional<CertificateStateHistoryEntry> sent = states.stream()
                .filter(state -> state.getState().equals(CertificateState.SENT))
                .findAny();

            response.setSentToRecipient(sent.map(CertificateStateHistoryEntry::getTimestamp).orElse(null));

            response.setSignedByName(certificate.getSigningDoctorName());

            response.setCareUnitHsaId(certificate.getCareUnitId());
            response.setCareUnitName(certificate.getCareUnitName());

            response.setCareGiverHsaId(certificate.getCareGiverId());

            try {
                ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());

                Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());

                response.setSignedByHsaId(utlatande.getGrundData().getSkapadAv().getPersonId());
                response.setCareGiverName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());

            } catch (ModuleException | ModuleNotFoundException e) {
                LOG.error("ModuleApi not working", e);
            }

            List<Recipient> recipients = recipientService.listRecipients(id);
            response.setNumberOfRecipients(recipients.size());

            addHistory(response, certificate);

            return response;
        } catch (InvalidCertificateException e) {
            LOG.error("Intyg not found", e);
        }

        return null;
    }

    private void addHistory(IntygInfoResponse response, Certificate certificate) {
        List<IntygInfoHistory> history = response.getHistory();

        // Signed by
        IntygInfoHistory signedBy = new IntygInfoHistory();
        signedBy.setDate(certificate.getSignedDate());
        signedBy.setText("Intyget signerades av " + certificate.getSigningDoctorName());
        history.add(signedBy);

        certificate.getStates().forEach(state -> {
            String text = null;

            switch (state.getState()) {
                case SENT:
                    text = "Intyget skickades till " + state.getTarget();
                    break;
                case RECEIVED:
                    // TODO: Kolla om det ska finnas i MI?
                    text = "Intyget är tillgängligt för patienten via Mina intyg";
                    break;
                case CANCELLED:
                    text = "Intyget makulerades";
                    break;
                default:
                    break;
            }

            if (text != null) {
                history.add(new IntygInfoHistory(state.getTimestamp(), text));
            }
        });
    }
}
