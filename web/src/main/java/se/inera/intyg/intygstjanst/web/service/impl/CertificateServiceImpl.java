/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import com.google.common.annotations.VisibleForTesting;

import se.inera.intyg.common.support.integration.module.exception.*;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.*;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateServiceImpl implements CertificateService, ModuleContainerApi {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateServiceImpl.class);

    public static final String HVTARGET = "HV";

    @Autowired
    private CertificateDao certificateDao;

    @SuppressWarnings("unused")
    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @Autowired
    private CertificateSenderService senderService;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private SjukfallCertificateService sjukfallCertificateService;

    @Autowired
    @Value("${store.original.certificate}")
    private Boolean shouldStoreOriginalCertificate = true;

    @Override
    @Transactional(readOnly = true)
    public List<Certificate> listCertificatesForCitizen(Personnummer civicRegistrationNumber, List<String> certificateTypes,
            LocalDate fromDate, LocalDate toDate) throws MissingConsentException {
        assertConsent(civicRegistrationNumber);
        return certificateDao.findCertificate(civicRegistrationNumber, certificateTypes, fromDate, toDate, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificate> listCertificatesForCare(Personnummer civicRegistrationNumber, List<String> careUnits) {
        return certificateDao.findCertificate(civicRegistrationNumber, null, null, null, careUnits);
    }

    @Override
    @Transactional(readOnly = true, noRollbackFor = { PersistenceException.class })
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
    @Transactional(readOnly = true, noRollbackFor = { PersistenceException.class })
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

        if (certificate.isAlreadySent(recipientId)) {
            return SendStatus.ALREADY_SENT;
        }

        // Do send the certificate
        senderService.sendCertificate(certificate, recipientId);

        // Update the certificate
        setCertificateState(civicRegistrationNumber, certificateId, recipientId, CertificateState.SENT, null);

        return SendStatus.OK;
    }

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateException.class, PersistenceException.class })
    public void setCertificateState(Personnummer civicRegistrationNumber, String certificateId, String target,
            CertificateState state, LocalDateTime timestamp) throws InvalidCertificateException {
        try {
            certificateDao.updateStatus(certificateId, civicRegistrationNumber, state, target, timestamp);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, civicRegistrationNumber);
        }
    }

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateException.class, PersistenceException.class })
    public void setCertificateState(String certificateId, String target,
            CertificateState state, LocalDateTime timestamp) throws InvalidCertificateException {
        try {
            certificateDao.updateStatus(certificateId, state, target, timestamp);
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificateId, null);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = { InvalidCertificateException.class,
            CertificateRevokedException.class })
    public Certificate revokeCertificate(Personnummer civicRegistrationNumber, String certificateId)
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

        setCertificateState(civicRegistrationNumber, certificateId, HVTARGET, CertificateState.CANCELLED, null);

        return certificate;
    }

    @Override
    @Transactional
    public void certificateReceived(CertificateHolder certificateHolder) throws CertificateAlreadyExistsException, InvalidCertificateException {
        LOG.debug("Certificate received {}", certificateHolder.getId());
        Certificate certificate = storeCertificate(certificateHolder);
        LOG.debug("Certificate stored {}", certificateHolder.getId());
        monitoringLogService.logCertificateRegistered(certificate.getId(), certificate.getType(), certificate.getCareUnitId());

        if (certificateHolder.isWireTapped()) {
            Personnummer personnummer = certificateHolder.getCivicRegistrationNumber();
            String certificateId = certificateHolder.getId();
            final String recipient = "FK";
            setCertificateState(personnummer, certificateId, recipient, CertificateState.SENT,
                    new LocalDateTime());
            monitoringLogService.logCertificateSentAndNotifiedByWiretapping(certificate.getId(), certificate.getType(), certificate.getCareUnitId(),
                    recipient);
        }

        statisticsService.created(certificate);

        /**
         * TODO INTYG-2042: This code below should be uncommented and used immediately when the statistics service has
         * been updated accordingly.
         * String transformedXml = certificateReceivedForStatistics(certificateHolder);
         * statisticsService.created(transformedXml, certificate.getId(), certificate.getType(),
         * certificate.getCareUnitId());
         **/

        sjukfallCertificateService.created(certificate);

    }

    /**
     * TODO INTYG-2042: This code should be uncommented and used immediately when the statistics service has been
     * updated accordingly.
     * private String certificateReceivedForStatistics(CertificateHolder certificateHolder)
     * throws CertificateAlreadyExistsException, InvalidCertificateException {
     * try {
     * ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateHolder.getType());
     * String resultXml = moduleApi.transformToStatisticsService(certificateHolder.getOriginalCertificate());
     * return resultXml;
     * } catch (ModuleNotFoundException | ModuleException e) {
     * LOG.error("Module not found for certificate of type {}", certificateHolder.getType());
     * throw Throwables.propagate(e);
     * }
     * }
     **/

    @VisibleForTesting
    Certificate storeCertificate(CertificateHolder certificateHolder) throws CertificateAlreadyExistsException,
            InvalidCertificateException {
        Certificate certificate = ConverterUtil.toCertificate(certificateHolder);

        // ensure that certificate does not exist yet
        try {
            checkForExistingCertificate(certificate.getId(), certificate.getCivicRegistrationNumber());
        } catch (PersistenceException e) {
            throw new InvalidCertificateException(certificate.getId(), certificate.getCivicRegistrationNumber());
        }

        // add initial RECEIVED state using current time as receiving timestamp
        CertificateStateHistoryEntry state = new CertificateStateHistoryEntry(HVTARGET, CertificateState.RECEIVED,
                new LocalDateTime());
        certificate.addState(state);
        certificateDao.store(certificate);
        storeOriginalCertificate(certificateHolder.getOriginalCertificate(), certificate);
        return certificate;
    }

    @Override
    @Transactional(readOnly = true, noRollbackFor = { PersistenceException.class })
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
