/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import jakarta.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.CertificateExportService;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateXmlDTO;

@Service
public class CertificateExportServiceImpl implements CertificateExportService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateExportServiceImpl.class);

    private static final String TEXTS_LOCATION = "classpath:texts/*";
    private static final String XML_FILE_EXTENSION = ".xml";
    private static final String TYPE_ATTRIBUTE = "typ";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String ACTIVATION_DATE_ATTRIBUTE = "giltigFrom";

    private final CertificateRepository certificateRepository;
    private final ArendeRepository arendeRepository;
    private final PathMatchingResourcePatternResolver resourceResolver;
    private final ApprovedReceiverDao approvedReceiverDao;
    private final RelationDao relationDao;
    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final CertificateDao certificateDao;

    public CertificateExportServiceImpl(CertificateRepository certificateRepository, ArendeRepository arendeRepository,
        PathMatchingResourcePatternResolver resourceResolver, ApprovedReceiverDao approvedReceiverDao, RelationDao relationDao,
        SjukfallCertificateDao sjukfallCertificateDao, CertificateDao certificateDao) {
        this.certificateRepository = certificateRepository;
        this.arendeRepository = arendeRepository;
        this.resourceResolver = resourceResolver;
        this.approvedReceiverDao = approvedReceiverDao;
        this.relationDao = relationDao;
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.certificateDao = certificateDao;
    }

    @Override
    public List<CertificateTextDTO> getCertificateTexts() {
        try {
            final var resources = resourceResolver.getResources(TEXTS_LOCATION);
            final var textFiles = Arrays.stream(resources).filter(this::isTextFile).collect(Collectors.toList());
            return getCertificateTexts(textFiles);
        } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
            LOG.error("Failure fetching certificate texts.", e);
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public CertificateExportPageDTO getCertificateExportPage(String careProviderId, int page, int size) {
        final var pageable = PageRequest.of(page, size, Sort.by(Direction.ASC, "signedDate", "id"));
        final var certificatePage = certificateRepository.findCertificatesForCareProvider(careProviderId, pageable);
        final var certificates = certificatePage.getContent();
        final var totalCertificates = certificatePage.getTotalElements();
        final var totalRevoked = certificateRepository.findTotalRevokedForCareProvider(careProviderId);
        final var certificateXmls = getCertificateXmls(certificates);
        final var certificateCount = certificatePage.getNumberOfElements();
        return new CertificateExportPageDTO(careProviderId, page, certificateCount, totalCertificates, totalRevoked, certificateXmls);
    }

    @Override
    public void eraseCertificates(String careProviderId, int erasePageSize) {
        final var erasePageable = PageRequest.of(0, erasePageSize, Sort.by(Direction.ASC, "signedDate", "id"));
        Page<String> certificateIdPage = Page.empty();
        int erasedMessagesTotal = 0;
        int erasedSjukfallTotal = 0;
        int erasedCertificatesTotal = 0;
        int erasedCertificates = 0;

        try {
            do {
                erasedCertificates = 0;
                certificateIdPage = certificateRepository.findCertificateIdsForCareProvider(careProviderId, erasePageable);
                final var certificateIds = certificateIdPage.getContent();

                if (certificateIds.isEmpty()) {
                    break;
                }

                LOG.info("Starting batch erasure of {} certificates for care provider {}.", certificateIds.size(), careProviderId);

                approvedReceiverDao.eraseApprovedReceivers(certificateIds, careProviderId);
                relationDao.eraseCertificateRelations(certificateIds, careProviderId);
                erasedMessagesTotal += eraseMessages(certificateIds, careProviderId);
                erasedSjukfallTotal += sjukfallCertificateDao.eraseCertificates(certificateIds, careProviderId);
                erasedCertificates = certificateDao.eraseCertificates(certificateIds, careProviderId);
                erasedCertificatesTotal += erasedCertificates;

                LOG.info("Completed batch erasure of {} certificates for care provider {}. Certificates remaining: {}.",
                    certificateIds.size(), careProviderId, certificateIdPage.getTotalElements() - erasedCertificates);

            } while (certificateIdPage.hasNext());

            LOG.info("Successfully completed erasure of certificates for care provider {}. Total number of erased certificates: {}, "
                    + "sjukfallCertificates: {}, messages: {}.", careProviderId, erasedCertificatesTotal, erasedSjukfallTotal,
                erasedMessagesTotal);

        } catch (Exception e) {
            LOG.error("Error erasing certificates for care provider {}. Number of erased certificates: {}, sjukfallCertificates: {}, "
                    + "messages: {}. Certificates remaining: {}.", careProviderId, erasedCertificatesTotal, erasedSjukfallTotal,
                erasedMessagesTotal, certificateIdPage.getTotalElements() - erasedCertificates, e);
            throw e;
        }
    }

    private List<CertificateTextDTO> getCertificateTexts(List<Resource> textFiles) throws IOException, ParserConfigurationException,
        SAXException, TransformerException {
        final var certificateTexts = new ArrayList<CertificateTextDTO>();
        for (final var textFile : textFiles) {
            final var textDocument = parseTextFile(textFile);
            if (isActive(textDocument)) {
                final var type = getTextAttribute(textDocument, TYPE_ATTRIBUTE);
                final var version = getTextAttribute(textDocument, VERSION_ATTRIBUTE);
                final var xml = getTextXml(textDocument);
                certificateTexts.add(new CertificateTextDTO(type, version, xml));
            }
        }

        return certificateTexts;
    }

    private Document parseTextFile(Resource textFile) throws IOException, ParserConfigurationException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(textFile.getInputStream());
    }

    private boolean isActive(Document textDocument) {
        final var activationDate = getTextAttribute(textDocument, ACTIVATION_DATE_ATTRIBUTE);
        final var activationLocalDate = LocalDate.parse(activationDate);
        return activationLocalDate.isBefore(LocalDate.now()) || activationLocalDate.isEqual(LocalDate.now());
    }

    private String getTextAttribute(Document textDocument, String attribute) {
        return textDocument.getDocumentElement().getAttribute(attribute);
    }

    private boolean isTextFile(Resource resource) {
        return resource.getFilename() != null && resource.getFilename().endsWith(XML_FILE_EXTENSION);
    }

    private String getTextXml(Document textDocument) throws TransformerException {
        final var stringWriter = new StringWriter();
        final var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(textDocument), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    private List<CertificateXmlDTO> getCertificateXmls(List<Certificate> certificates) {
        return certificates.stream()
            .map(certificate -> new CertificateXmlDTO(
                certificate.getId(),
                certificate.getCertificateMetaData().isRevoked(),
                certificate.getOriginalCertificate().getDocument()))
            .collect(Collectors.toList());
    }

    private int eraseMessages(List<String> certificateIds, String careProviderId) {
        final var erasedArendeCount = arendeRepository.eraseArendenByCertificateIds(certificateIds);
        LOG.debug("Erased {} Arenden for care provider {}.", erasedArendeCount, careProviderId);
        return erasedArendeCount;
    }
}
