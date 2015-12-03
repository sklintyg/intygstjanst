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
package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.*;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateServiceImpl implements CertificateService, ModuleContainerApi {

    public static final String MI = "MI";

    @Autowired
    private CertificateDao certificateDao;

    @Autowired
    private CertificateSenderService senderService;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    @Value("${store.original.certificate}")
    private Boolean shouldStoreOriginalCertificate = true;


    // - - - - - Public methods - - - - - //

    @Override
    public List<Certificate> listCertificatesForCitizen(Personnummer civicRegistrationNumber, List<String> certificateTypes,
            LocalDate fromDate, LocalDate toDate) throws MissingConsentException {
        assertConsent(civicRegistrationNumber);
        return certificateDao.findCertificate(civicRegistrationNumber, certificateTypes, fromDate, toDate, null);
    }

    @Override
    public List<Certificate> listCertificatesForCare(Personnummer civicRegistrationNumber, List<String> careUnits) {
        return certificateDao.findCertificate(civicRegistrationNumber, null, null, null, careUnits);
    }

    @Override
    public Certificate getCertificateForCitizen(Personnummer civicRegistrationNumber, String certificateId) throws InvalidCertificateException,
            CertificateRevokedException,
            MissingConsentException {

        assertConsent(civicRegistrationNumber);

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

        return certificate;
    }

    @Override
    public Certificate getCertificateForCare(String certificateId) throws InvalidCertificateException {

        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(null, certificateId);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, null);
        }

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, null);
        }

        return certificate;
    }

    @Override
    @Transactional
    public SendStatus sendCertificate(Personnummer civicRegistrationNumber, String certificateId, String recipientId)
            throws InvalidCertificateException, CertificateRevokedException, RecipientUnknownException {

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
        if (certificate.isAlreadySent(recipientId)) {
            sendStatus = SendStatus.ALREADY_SENT;
        }

        // Do send the certificate
        senderService.sendCertificate(certificate, recipientId);

        // Update the certificate
        setCertificateState(civicRegistrationNumber, certificateId, recipientId, CertificateState.SENT, null);

        return sendStatus;
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

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateException.class })
    public void setCertificateState(Personnummer civicRegistrationNumber, String certificateId, String target,
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
    public Certificate revokeCertificate(Personnummer civicRegistrationNumber, String certificateId, RevokeType revokeData)
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

    @Override
    public void setArchived(String certificateId, Personnummer civicRegistrationNumber, String archivedState) throws InvalidCertificateException {
        try {
            certificateDao.setArchived(certificateId, civicRegistrationNumber, archivedState);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }
    }

    @Override
    @Transactional
    public void certificateReceived(CertificateHolder certificateHolder) throws CertificateAlreadyExistsException, InvalidCertificateException {
        Certificate certificate = storeCertificate(certificateHolder);
        monitoringLogService.logCertificateRegistered(certificate.getId(), certificate.getType(), certificate.getCareUnitId());

        if (certificateHolder.isWireTapped()) {
            Personnummer personnummer = certificateHolder.getCivicRegistrationNumber();
            String certificateId = certificateHolder.getId();
            final String recipient = "FK";
            setCertificateState(personnummer, certificateId, recipient, CertificateState.SENT,
                    new LocalDateTime());
            monitoringLogService.logCertificateSentAndNotifiedByWiretapping(certificate.getId(), certificate.getType(), certificate.getCareUnitId(), recipient);
        }
        statisticsService.created(certificate);
    }

    @Override
    public CertificateHolder getCertificate(String certificateId, Personnummer personId, boolean checkConsent) throws InvalidCertificateException {
        if (checkConsent && personId != null && !consentService.isConsent(personId)) {
            throw new MissingConsentException(personId);
        }

        Certificate certificate = null;
        try {
            certificate = getCertificateInternal(personId, certificateId);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, personId);
        }

        if (certificate == null) {
            throw new InvalidCertificateException(certificateId, personId);
        }

        return ConverterUtil.toCertificateHolder(certificate);
    }

    // - - - - - Private methods - - - - - //

    private void assertConsent(Personnummer civicRegistrationNumber) throws MissingConsentException {
        if (civicRegistrationNumber == null || StringUtils.isEmpty(civicRegistrationNumber.getPersonnummer())) {
            throw new IllegalArgumentException("Invalid/missing civicRegistrationNumber");
        }

        if (!consentService.isConsent(civicRegistrationNumber)) {
            throw new MissingConsentException(civicRegistrationNumber);
        }
    }

    private void checkForExistingCertificate(String certificateId, Personnummer personnummer) throws CertificateAlreadyExistsException,
            PersistenceException {
        if (certificateDao.getCertificate(personnummer, certificateId) != null) {
            throw new CertificateAlreadyExistsException(certificateId);
        }
    }

    private Certificate getCertificateInternal(Personnummer civicRegistrationNumber, String certificateId) throws PersistenceException {
        return certificateDao.getCertificate(civicRegistrationNumber, certificateId);
    }

    private void sendRevokeMessagesToRecipients(Certificate certificate, RevokeType revokeData) {
        HashSet<String> recipientsFound = new HashSet<>();

        for (CertificateStateHistoryEntry event : certificate.getStates()) {
            if (event.getState().equals(CertificateState.SENT)) {
                String recipient = event.getTarget();
                if (recipientsFound.add(recipient)) {
                    senderService.sendCertificateRevocation(certificate, recipient, revokeData);
                }
            }
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

}
