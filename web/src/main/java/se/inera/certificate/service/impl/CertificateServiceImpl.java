/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.service.impl;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.InvalidCertificateIdentifierException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.exception.ModuleCallFailedException;
import se.inera.certificate.integration.util.RestUtils;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.model.dao.OriginalCertificate;
import se.inera.certificate.schema.adapter.PartialAdapter;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.ConsentService;
import se.inera.certificate.validate.ValidationException;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final int OK = 200;

    private static final int BAD_REQUEST = 400;

    private static final int NOT_FOUND = 404;

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    private static final Comparator<CertificateStateHistoryEntry> SORTER = new Comparator<CertificateStateHistoryEntry>() {

        @Override
        public int compare(CertificateStateHistoryEntry e1, CertificateStateHistoryEntry e2) {
            return e2.getTimestamp().compareTo(e1.getTimestamp());
        }
    };

    public static final String MI = "MI";

    @Autowired
    private CertificateDao certificateDao;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private CertificateSenderService senderService;

    @Autowired
    @Value("${store.original.certificate}")
    private Boolean shouldStoreOriginalCertificate = true;

    @Override
    public List<Certificate> listCertificates(String civicRegistrationNumber, List<String> certificateTypes,
            LocalDate fromDate, LocalDate toDate) {
        assertConsent(civicRegistrationNumber);
        return fixDeletedStatus(certificateDao.findCertificate(civicRegistrationNumber, certificateTypes, fromDate,
                toDate));
    }

    @Override
    public Certificate getCertificate(String civicRegistrationNumber, String id) {

        // if personnummer is provided, we check for given consent
        if (civicRegistrationNumber != null) {
            assertConsent(civicRegistrationNumber);
        }

        Certificate certificate = getCertificateInternal(civicRegistrationNumber, id);

        if (certificate == null) {
            throw new InvalidCertificateException(id, civicRegistrationNumber);
        }

        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(id);
        }

        return certificate;
    }

    private Certificate getCertificateInternal(String civicRegistrationNumber, String id) {
        return fixDeletedStatus(certificateDao.getCertificate(civicRegistrationNumber, id));
    }

    private String unmarshall(String type, String transportXml) {

        ModuleRestApi endpoint = moduleRestApiFactory.getModuleRestService(type);

        Response response = endpoint.unmarshall(transportXml);

        String entityContent = RestUtils.entityAsString(response);

        switch (response.getStatus()) {
        case NOT_FOUND:
            String errorMessage = "Module of type " + type + " not found, 404!";
            LOGGER.error(errorMessage);
            throw new ModuleCallFailedException("Module of type " + type + " not found, 404!", response);
        case BAD_REQUEST:
            throw new ValidationException(entityContent);
        case OK:
            return entityContent;
        default:
            String message = "Failed to validate certificate for certificate type '" + type
                    + "'. HTTP status code is " + response.getStatus();
            LOGGER.error(message);
            throw new ModuleCallFailedException(message, response);
        }
    }

    private Utlatande convertToCommonUtlatande(String externalJson) {
        try {
            return objectMapper.readValue(externalJson, Utlatande.class);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    @Transactional
    public Certificate storeCertificate(String xml, String type) {

        String externalJson = unmarshall(type, xml);

        Utlatande utlatande = convertToCommonUtlatande(externalJson);

        // turn a lakarutlatande into a certificate entity
        Certificate certificate = createCertificate(utlatande, externalJson);

        // add initial RECEIVED state using current time as receiving timestamp
        CertificateStateHistoryEntry state = new CertificateStateHistoryEntry(MI, CertificateState.RECEIVED,
                new LocalDateTime());
        certificate.addState(state);

        certificateDao.store(certificate);

        storeOriginalCertificate(xml, certificate);

        return certificate;
    }

    /**
     * 
     * @param utlatandeXml
     *            the received certificate utlatande xml
     * @param certificate
     *            the {@link Certificate} generated from the utlatandeXml, or <code>null</code> if unknown.
     */
    private void storeOriginalCertificate(String utlatandeXml, Certificate certificate) {
        if (shouldStoreOriginalCertificate) {
            OriginalCertificate original = new OriginalCertificate(LocalDateTime.now(), utlatandeXml, certificate);
            certificateDao.storeOriginalCertificate(original);
        }
    }

    @Override
    @Transactional
    public SendStatus sendCertificate(String civicRegistrationNumber, String certificateId, String target) {
        Certificate certificate = getCertificateInternal(civicRegistrationNumber, certificateId);

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }
        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(certificateId);
        }

        SendStatus sendStatus = SendStatus.OK;
        if (certificate.wasSentToTarget("FK")) {
            sendStatus = SendStatus.ALREADY_SENT;
        }
        senderService.sendCertificate(certificate, target);
        setCertificateState(civicRegistrationNumber, certificateId, target, CertificateState.SENT, null);
        return sendStatus;
    }

    private Certificate createCertificate(Utlatande utlatande, String externalJson) {
        Certificate certificate = new Certificate(utlatande.getId().getRoot(), externalJson);

        certificate.setType(utlatande.getTyp().getCode());
        certificate.setSigningDoctorName(utlatande.getSkapadAv().getNamn());
        certificate.setSignedDate(utlatande.getSigneringsdatum());

        if (utlatande.getSkapadAv() != null && utlatande.getSkapadAv().getVardenhet() != null) {
            certificate.setCareUnitName(utlatande.getSkapadAv().getVardenhet().getNamn());
        }

        certificate.setCivicRegistrationNumber(utlatande.getPatient().getId().getExtension());
        certificate.setValidFromDate(PartialAdapter.printPartial(utlatande.getValidFromDate()));
        certificate.setValidToDate(PartialAdapter.printPartial(utlatande.getValidToDate()));

        return certificate;
    }

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateIdentifierException.class })
    public void setCertificateState(String civicRegistrationNumber, String certificateId, String target,
            CertificateState state, LocalDateTime timestamp) {
        certificateDao.updateStatus(certificateId, civicRegistrationNumber, state, target, timestamp);
    }

    @Override
    public Utlatande getLakarutlatande(Certificate certificate) {
        try {
            return objectMapper.readValue(certificate.getDocument(), Utlatande.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse document for " + certificate.getId(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = { InvalidCertificateException.class,
            CertificateRevokedException.class })
    public Certificate revokeCertificate(String civicRegistrationNumber, String certificateId) {
        Certificate certificate = getCertificateInternal(civicRegistrationNumber, certificateId);

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }

        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(certificateId);
        }
        setCertificateState(civicRegistrationNumber, certificateId, "FK", CertificateState.CANCELLED, null);
        return certificate;
    }

    private void assertConsent(String civicRegistrationNumber) {
        if (!consentService.isConsent(civicRegistrationNumber)) {
            throw new MissingConsentException(civicRegistrationNumber);
        }
    }

    private List<Certificate> fixDeletedStatus(List<Certificate> certificates) {
        for (Certificate certificate : certificates) {
            fixDeletedStatus(certificate);
        }
        return certificates;
    }

    private Certificate fixDeletedStatus(Certificate certificate) {
        if (certificate != null) {
            List<CertificateStateHistoryEntry> states = new ArrayList<>(certificate.getStates());
            Collections.sort(states, SORTER);
            certificate.setDeleted(isDeleted(states));
        }
        return certificate;
    }

    private Boolean isDeleted(List<CertificateStateHistoryEntry> entries) {
        for (CertificateStateHistoryEntry entry : entries) {
            switch (entry.getState()) {
            case DELETED:
                return true;
            case RESTORED:
                return false;
            default:
            }
        }
        return false;
    }
}
