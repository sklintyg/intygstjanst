/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.integration.CitizenController;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.IntygInfoService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

@Service
public class IntygInfoServiceImpl implements IntygInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygInfoServiceImpl.class);

    @Autowired
    private CertificateService certificateService;
    @Autowired
    private RecipientService recipientService;
    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private RelationService relationService;

    @Override
    public Optional<ItIntygInfo> getIntygInfo(String id) {

        try {
            Certificate certificate = certificateService.getCertificateForCare(id);

            ItIntygInfo response = new ItIntygInfo();

            // General info
            response.setIntygId(certificate.getId());
            response.setIntygType(certificate.getType());
            response.setIntygVersion(certificate.getTypeVersion());

            response.setTestCertificate(certificate.isTestCertificate());

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

            List<Recipient> recipients = recipientService.listRecipients(id);
            response.setNumberOfRecipients(recipients.size());

            try {
                ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());

                Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());

                response.setSignedByHsaId(utlatande.getGrundData().getSkapadAv().getPersonId());
                response.setCareGiverName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());

                ModuleEntryPoint entryPoint = moduleRegistry.getModuleEntryPoint(certificate.getType());

                if (!StringUtils.isEmpty(entryPoint.getDefaultRecipient())) {
                    if (response.getNumberOfRecipients() == 0) {
                        response.setNumberOfRecipients(1);
                    }
                }

            } catch (ModuleException | ModuleNotFoundException e) {
                LOG.error("ModuleApi not working", e);
            }

            addEvents(response, certificate);

            return Optional.of(response);
        } catch (InvalidCertificateException e) {
            LOG.info("Intyg not found", e);
        }

        return Optional.empty();
    }

    private void addEvents(ItIntygInfo response, Certificate certificate) {
        List<IntygInfoEvent> events = response.getEvents();

        // Signed by
        IntygInfoEvent signedBy = new IntygInfoEvent(Source.INTYGSTJANSTEN, certificate.getSignedDate(), IntygInfoEventType.IS004);
        signedBy.addData("name", certificate.getSigningDoctorName());
        signedBy.addData("hsaId", response.getSignedByHsaId());
        events.add(signedBy);

        certificate.getStates().forEach(state -> {
            IntygInfoEvent event = null;

            switch (state.getState()) {
                case SENT:
                    event = new IntygInfoEvent(Source.INTYGSTJANSTEN, state.getTimestamp(), IntygInfoEventType.IS006);
                    event.addData("intygsmottagare", state.getTarget());
                    break;
                case RECEIVED:
                    if (!CitizenController.EXCLUDED_CITIZEN_CERTIFICATES.contains(certificate.getType())) {
                        event = new IntygInfoEvent(Source.INTYGSTJANSTEN, state.getTimestamp(), IntygInfoEventType.IS005);
                    }
                    break;
                case CANCELLED:
                    event = new IntygInfoEvent(Source.INTYGSTJANSTEN, state.getTimestamp(), IntygInfoEventType.IS009);
                    break;
                default:
                    break;
            }

            if (event != null) {
                events.add(event);
            }
        });


        List<Relation> relations = relationService.getChildRelations(certificate.getId());

        relations.forEach(relation -> {
            IntygInfoEvent event = null;

            RelationKod kod = RelationKod.fromValue(relation.getRelationKod());

            switch (kod) {
                case ERSATT:
                    event = new IntygInfoEvent(Source.INTYGSTJANSTEN, relation.getCreated(), IntygInfoEventType.IS008);
                    break;
                case KOMPLT:
                    event = new IntygInfoEvent(Source.INTYGSTJANSTEN, relation.getCreated(), IntygInfoEventType.IS014);
                    break;
                case FRLANG:
                    event = new IntygInfoEvent(Source.INTYGSTJANSTEN, relation.getCreated(), IntygInfoEventType.IS007);
                    break;
                default:

            }

            if (event != null) {
                event.addData("intygsId", relation.getFromIntygsId());
                events.add(event);
            }
        });
    }
}
