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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.CertificateValidationException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.exception.MissingModuleException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.Vardenhet;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.model.dao.OriginalCertificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.modules.support.api.exception.ModuleValidationException;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.ConsentService;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private ModuleApiFactory moduleApiFactory;

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
    public List<Certificate> listCertificatesForCitizen(String civicRegistrationNumber, List<String> certificateTypes,
            LocalDate fromDate, LocalDate toDate) throws MissingConsentException {
        assertConsent(civicRegistrationNumber);
        return fixDeletedStatus(certificateDao.findCertificate(civicRegistrationNumber, certificateTypes, fromDate,
                toDate, null));
    }

    @Override
    public List<Certificate> listCertificatesForCare(String civicRegistrationNumber, List<String> careUnits) {
        return fixDeletedStatus(certificateDao.findCertificate(civicRegistrationNumber, null, null, null, careUnits));
    }

    @Override
    public Certificate getCertificateForCitizen(String civicRegistrationNumber, String id) throws InvalidCertificateException, CertificateRevokedException,
            MissingConsentException {

        assertConsent(civicRegistrationNumber);

        Certificate certificate = getCertificateInternal(civicRegistrationNumber, id);

        if (certificate == null) {
            throw new InvalidCertificateException(id, civicRegistrationNumber);
        }

        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(id);
        }

        return certificate;
    }

    @Override
    public Certificate getCertificateForCare(String id) throws InvalidCertificateException {

        Certificate certificate = getCertificateInternal(null, id);

        if (certificate == null) {
            throw new InvalidCertificateException(id, null);
        }

        return certificate;
    }

    private Certificate getCertificateInternal(String civicRegistrationNumber, String id) throws InvalidCertificateException {
        return fixDeletedStatus(certificateDao.getCertificate(civicRegistrationNumber, id));
    }

    private ExternalModelResponse unmarshallAndValidate(String type, String transportXml) throws CertificateValidationException {
        try {
            ModuleEntryPoint endpoint = moduleApiFactory.getModuleEntryPoint(type);
            ExternalModelResponse response = endpoint.getModuleApi().unmarshall(new TransportModelHolder(transportXml));

            return response;

        } catch (ModuleNotFoundException e) {
            String message = String.format("The module '%s' was not found - not registered in application", type);
            LOGGER.error(message);
            throw new MissingModuleException(message, e);

        } catch (ModuleValidationException e) {
            throw new CertificateValidationException(e.getValidationEntries());

        } catch (ModuleException e) {
            String message = String.format("Failed to validate certificate for certificate type '%s'", type);
            LOGGER.error(message);
            throw new ServerException(message, e);
        }
    }

    @Override
    @Transactional
    public Certificate storeCertificate(String xml, String type, boolean wiretapped) throws CertificateAlreadyExistsException,
            InvalidCertificateException, CertificateValidationException {

        ExternalModelResponse externalModel = unmarshallAndValidate(type, xml);

        // turn a lakarutlatande into a certificate entity
        Certificate certificate = createCertificate(externalModel);
        certificate.setWiretapped(wiretapped);

        // ensure that certificate does not exist yet
        checkForExistingCertificate(certificate.getId(), certificate.getCivicRegistrationNumber());

        // add initial RECEIVED state using current time as receiving timestamp
        LocalDateTime now = new LocalDateTime();
        CertificateStateHistoryEntry state = new CertificateStateHistoryEntry(MI, CertificateState.RECEIVED, now);
        certificate.addState(state);
        // If wiretapped, also add SENT state,
        if (wiretapped) {
            state = new CertificateStateHistoryEntry("FK", CertificateState.SENT, now);
            certificate.addState(state);
        }

        certificateDao.store(certificate);

        storeOriginalCertificate(xml, certificate);

        return certificate;
    }

    private void checkForExistingCertificate(String certificateId, String personnummer) throws CertificateAlreadyExistsException,
            InvalidCertificateException {
        if (certificateDao.getCertificate(personnummer, certificateId) != null) {
            throw new CertificateAlreadyExistsException(certificateId);
        }
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
    public SendStatus sendCertificate(String civicRegistrationNumber, String certificateId, String target)
            throws InvalidCertificateException, CertificateRevokedException {
        Certificate certificate = getCertificateInternal(civicRegistrationNumber, certificateId);

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }
        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(certificateId);
        }

        SendStatus sendStatus = SendStatus.OK;
        if (certificate.wasSentToTarget(target)) {
            sendStatus = SendStatus.ALREADY_SENT;
        }
        senderService.sendCertificate(certificate, target);
        setCertificateState(civicRegistrationNumber, certificateId, target, CertificateState.SENT, null);
        return sendStatus;
    }

    private Certificate createCertificate(ExternalModelResponse externalModel) {
        Utlatande utlatande = externalModel.getExternalModel();
        String id = utlatande.getId().getExtension() != null ? utlatande.getId().getExtension() : utlatande.getId().getRoot();
        Certificate certificate = new Certificate(id, externalModel.getExternalModelJson());

        certificate.setType(utlatande.getTyp().getCode());
        certificate.setSigningDoctorName(utlatande.getSkapadAv().getNamn());
        certificate.setSignedDate(utlatande.getSigneringsdatum());

        if (utlatande.getSkapadAv() != null && utlatande.getSkapadAv().getVardenhet() != null) {
            Vardenhet vardEnhet = utlatande.getSkapadAv().getVardenhet();
            certificate.setCareUnitName(vardEnhet.getNamn());
            if (vardEnhet.getId() != null) {
                certificate.setCareUnitId(vardEnhet.getId().getExtension());
            }
            if (vardEnhet.getVardgivare() != null && vardEnhet.getVardgivare().getId() != null) {
                certificate.setCareGiverId(vardEnhet.getVardgivare().getId().getExtension());
            }
        }

        certificate.setCivicRegistrationNumber(utlatande.getPatient().getId().getExtension());
        certificate.setValidFromDate(utlatande.getValidFromDate() != null ? utlatande.getValidFromDate().toString() : null);
        certificate.setValidToDate(utlatande.getValidToDate() != null ? utlatande.getValidToDate().toString() : null);

        return certificate;
    }

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateException.class })
    public void setCertificateState(String civicRegistrationNumber, String certificateId, String target,
            CertificateState state, LocalDateTime timestamp) throws InvalidCertificateException {
        certificateDao.updateStatus(certificateId, civicRegistrationNumber, state, target, timestamp);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = { InvalidCertificateException.class,
            CertificateRevokedException.class })
    public Certificate revokeCertificate(String civicRegistrationNumber, String certificateId) throws InvalidCertificateException,
            CertificateRevokedException {
        Certificate certificate = getCertificateInternal(civicRegistrationNumber, certificateId);

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }

        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(certificateId);
        }

        String type = certificate.getType();
        setCertificateState(civicRegistrationNumber, certificateId, type, CertificateState.CANCELLED, null);

        return certificate;
    }

    private void assertConsent(String civicRegistrationNumber) throws MissingConsentException {

        if (StringUtils.isEmpty(civicRegistrationNumber)) {
            throw new IllegalArgumentException("Invalid/missing civicRegistrationNumber");
        }

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
