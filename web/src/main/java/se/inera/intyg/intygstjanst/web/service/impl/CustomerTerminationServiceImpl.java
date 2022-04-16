/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import java.io.StringWriter;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.web.service.CustomerTerminationService;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateXmlDTO;

@Service
public class CustomerTerminationServiceImpl implements CustomerTerminationService {

    private static final String TEXTS_LOCATION = "classpath:texts/*";
    private static final String TYPE_ATTRIBUTE = "typ";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String XML_FILE_EXTENSION = ".xml";

    private final CertificateRepository certificateRepository;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public CustomerTerminationServiceImpl(CertificateRepository certificateRepository,
        PathMatchingResourcePatternResolver resourceResolver) {
        this.certificateRepository = certificateRepository;
        this.resourceResolver = resourceResolver;
    }

    @Override
    public List<CertificateTextDTO> getCertificateTexts() throws IOException, ParserConfigurationException, TransformerException,
        SAXException {
        final var resources = resourceResolver.getResources(TEXTS_LOCATION);
        final var textFiles = Arrays.stream(resources).filter(this::isTextFile).collect(Collectors.toList());
        return getCertificateTexts(textFiles);
    }

    @Override
    public CertificateExportPageDTO getCertificateExportPage(String careProviderId, Pageable pageable) {
        final var certificatePage = certificateRepository.findCertificatesForCareProvider(careProviderId, pageable);
        final var certificates = certificatePage.getContent();
        final var totalCertificates = certificatePage.getTotalElements();
        final var totalRevoked = certificateRepository.findTotalRevokedForCareProvider(careProviderId);
        final var certificateXmls = getCertificateXmls(certificates);
        final var certificateXmlPage = new PageImpl<>(certificateXmls, pageable, certificatePage.getTotalElements());
        return new CertificateExportPageDTO(totalCertificates, totalRevoked, certificateXmlPage);
    }

    private ArrayList<CertificateTextDTO> getCertificateTexts(List<Resource> textFiles) throws IOException, ParserConfigurationException,
        SAXException, TransformerException {
        final var certificateTexts = new ArrayList<CertificateTextDTO>();
        for (final var textFile : textFiles) {
            final var textDocument = parseTextFile(textFile);
            final var type = getTextAttribute(textDocument, TYPE_ATTRIBUTE);
            final var version = getTextAttribute(textDocument, VERSION_ATTRIBUTE);
            final var xml = getTextXml(textDocument);
            certificateTexts.add(new CertificateTextDTO(type, version, xml));
        }

        return certificateTexts;
    }

    private Document parseTextFile(Resource textFile) throws IOException, ParserConfigurationException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(textFile.getInputStream());
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

    private ArrayList<CertificateXmlDTO> getCertificateXmls(List<Certificate> certificates) {
        final var certificateXmls = new ArrayList<CertificateXmlDTO>();
        for (final var certificate : certificates) {
            final var certificateId = certificate.getId();
            final var isRevoked = certificate.getCertificateMetaData().isRevoked();
            final var certificateXml = certificate.getOriginalCertificate().getDocument();
            certificateXmls.add(new CertificateXmlDTO(certificateId, isRevoked, certificateXml));
        }

        return certificateXmls;
    }
}
