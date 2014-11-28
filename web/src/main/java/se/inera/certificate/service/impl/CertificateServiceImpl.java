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
import java.util.HashSet;
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

import se.inera.certificate.exception.PersistenceException;
import se.inera.certificate.integration.converter.ConverterUtil;
import se.inera.certificate.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.certificate.integration.module.exception.CertificateRevokedException;
import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.integration.module.exception.MissingConsentException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.model.dao.OriginalCertificate;
import se.inera.certificate.modules.support.api.CertificateHolder;
import se.inera.certificate.modules.support.api.ModuleContainerApi;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.ConsentService;
import se.inera.certificate.service.StatisticsService;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateServiceImpl implements CertificateService, ModuleContainerApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateServiceImpl.class);

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
    private StatisticsService statisticsService;

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
    public Certificate getCertificateForCitizen(String civicRegistrationNumber, String id) throws InvalidCertificateException,
            CertificateRevokedException,
            MissingConsentException {

        assertConsent(civicRegistrationNumber);

        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(civicRegistrationNumber, id);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(id, civicRegistrationNumber);
        }

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

        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(null, id);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(null, id);
        }

        if (certificate == null) {
            throw new InvalidCertificateException(id, null);
        }

        return certificate;
    }

    private Certificate getCertificateInternal(String civicRegistrationNumber, String id) throws PersistenceException {
        return fixDeletedStatus(certificateDao.getCertificate(civicRegistrationNumber, id));
    }

    @Override
    @Transactional
    public Certificate storeCertificate(CertificateHolder certificateHolder) throws CertificateAlreadyExistsException,
            InvalidCertificateException {

        Certificate certificate = ConverterUtil.toCertificate(certificateHolder);

        // ensure that certificate does not exist yet
        try {
            checkForExistingCertificate(certificate.getId(), certificate.getCivicRegistrationNumber());
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificate.getId(), certificate.getCivicRegistrationNumber());
        }

        // add initial RECEIVED state using current time as receiving timestamp
        CertificateStateHistoryEntry state = new CertificateStateHistoryEntry(MI, CertificateState.RECEIVED,
                new LocalDateTime());
        certificate.addState(state);

        certificateDao.store(certificate);

        storeOriginalCertificate(certificateHolder.getOriginalCertificate(), certificate);

        return certificate;
    }

    private void checkForExistingCertificate(String certificateId, String personnummer) throws CertificateAlreadyExistsException,
            PersistenceException {
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
        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(civicRegistrationNumber, certificateId);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }

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

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateException.class })
    public void setCertificateState(String civicRegistrationNumber, String certificateId, String target,
            CertificateState state, LocalDateTime timestamp) throws InvalidCertificateException {
        try {
            certificateDao.updateStatus(certificateId, civicRegistrationNumber, state, target, timestamp);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = { InvalidCertificateException.class,
            CertificateRevokedException.class })
    public Certificate revokeCertificate(String civicRegistrationNumber, String certificateId, RevokeType revokeData)
            throws InvalidCertificateException,
            CertificateRevokedException {
        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(civicRegistrationNumber, certificateId);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }

        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(certificateId);
        }

        String type = certificate.getType();
        setCertificateState(civicRegistrationNumber, certificateId, type, CertificateState.CANCELLED, null);

        if (revokeData != null) {
            sendRevokeMessagesToRecipients(certificate, revokeData);
        }

        return certificate;
    }

    private void sendRevokeMessagesToRecipients(Certificate certificate, RevokeType revokeData) {
        HashSet<String> recipientsFound = new HashSet<>();

        for (CertificateStateHistoryEntry event : certificate.getStates()) {
            if (event.getState().equals(CertificateState.SENT)) {
                String recipient = event.getTarget();
                if (recipientsFound.add(recipient)) {
                    senderService.sendRevokeCertificateMessage(certificate, recipient, revokeData);
                }
            }
        }
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

    @Override
    @Transactional
    public void certificateReceived(CertificateHolder certificateHolder, boolean wireTapped) throws CertificateAlreadyExistsException, InvalidCertificateException {
        Certificate certificate = storeCertificate(certificateHolder);
        LOGGER.info(LogMarkers.MONITORING, certificateHolder.getId() + " registered");
        if (wireTapped) {
            String personnummer = certificateHolder.getCivicRegistrationNumber();
            String certificateId = certificateHolder.getId();
            setCertificateState(personnummer, certificateId, "FK", CertificateState.SENT,
                    new LocalDateTime());
            LOGGER.info(LogMarkers.MONITORING, certificateId + " marked as sent");
        }
        statisticsService.created(certificate);
    }

    @Override
    public CertificateHolder getCertificate(String certificateId, String personId) throws InvalidCertificateException, CertificateRevokedException {
        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(personId, certificateId);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, personId);
        }

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, personId);
        }

        if (personId != null && !consentService.isConsent(personId)) {
            throw new MissingConsentException(personId);
        }

        if (certificate.isRevoked()) {
            throw new CertificateRevokedException(certificateId);
        }

        return ConverterUtil.toCertificateHolder(certificate);
    }
}
