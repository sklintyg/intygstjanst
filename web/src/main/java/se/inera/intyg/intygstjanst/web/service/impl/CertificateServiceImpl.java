/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.web.service.*;
import se.inera.intyg.schemas.contract.Personnummer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateServiceImpl implements CertificateService, ModuleContainerApi {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private CertificateDao certificateDao;

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

    @Override
    @Transactional(readOnly = true)
    public List<Certificate> listCertificatesForCitizen(Personnummer civicRegistrationNumber, List<String> certificateTypes,
            LocalDate fromDate, LocalDate toDate) {
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
    public Certificate getCertificateForCitizen(Personnummer civicRegistrationNumber, String certificateId)
            throws InvalidCertificateException,
            CertificateRevokedException {

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

        setCertificateState(civicRegistrationNumber, certificateId,
                recipientService.getPrimaryRecipientHsvard().getId(), CertificateState.CANCELLED, null);

        return certificate;
    }

    @Override
    @Transactional
    public void certificateReceived(CertificateHolder certificateHolder)
            throws CertificateAlreadyExistsException, InvalidCertificateException {
        LOG.debug("Certificate received {}", certificateHolder.getId());
        Certificate certificate = storeCertificate(certificateHolder);
        LOG.debug("Certificate stored {}", certificateHolder.getId());
        monitoringLogService.logCertificateRegistered(certificate.getId(), certificate.getType(), certificate.getCareUnitId());

        String transformedXml = certificateReceivedForStatistics(certificateHolder);
        statisticsService.created(transformedXml, certificate.getId(), certificate.getType(),
                certificate.getCareUnitId());
        sjukfallCertificateService.created(certificate);
    }

    private String certificateReceivedForStatistics(CertificateHolder certificateHolder)
            throws CertificateAlreadyExistsException, InvalidCertificateException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateHolder.getType());
            return moduleApi.transformToStatisticsService(certificateHolder.getOriginalCertificate());
        } catch (ModuleNotFoundException | ModuleException e) {
            LOG.error("Module not found for certificate of type {}", certificateHolder.getType());
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void revokeCertificateForStatistics(Certificate certificate) {
        String certificateXml;
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType());
            certificateXml = moduleApi.transformToStatisticsService(certificate.getOriginalCertificate().getDocument());
        } catch (ModuleNotFoundException | ModuleException e) {
            LOG.error("Module not found for certificate of type {}", certificate.getType());
            throw Throwables.propagate(e);
        }
        statisticsService.revoked(certificateXml, certificate.getId(), certificate.getType(),
                certificate.getCareUnitId());

    }

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
        CertificateStateHistoryEntry state = new CertificateStateHistoryEntry(
                recipientService.getPrimaryRecipientHsvard().getId(), CertificateState.RECEIVED, LocalDateTime.now());
        certificate.addState(state);
        certificateDao.store(certificate);
        storeOriginalCertificate(certificateHolder.getOriginalCertificate(), certificate);
        return certificate;
    }

    @Override
    @Transactional(readOnly = true, noRollbackFor = { PersistenceException.class })
    public CertificateHolder getCertificate(String certificateId, Personnummer personId, boolean checkConsent)
            throws InvalidCertificateException {
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

    private void assertConsent(Personnummer civicRegistrationNumber) {
        if (civicRegistrationNumber == null || Strings.isNullOrEmpty(civicRegistrationNumber.getPersonnummer())) {
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
        OriginalCertificate original = new OriginalCertificate(LocalDateTime.now(), utlatandeXml, certificate);
        certificateDao.storeOriginalCertificate(original);
        certificate.setOriginalCertificate(original);
    }
}
