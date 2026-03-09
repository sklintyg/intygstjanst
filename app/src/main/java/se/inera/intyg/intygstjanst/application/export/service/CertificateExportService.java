/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.application.export.service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import se.inera.intyg.intygstjanst.application.export.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.application.export.dto.CertificateTextDTO;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator.EraseCertificatesAggregator;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator.ExportCertificateAggregator;

@Service
@RequiredArgsConstructor
public class CertificateExportService {

  private static final String TEXTS_LOCATION = "classpath:texts/*";
  private static final String XML_FILE_EXTENSION = ".xml";
  private static final String TYPE_ATTRIBUTE = "typ";
  private static final String VERSION_ATTRIBUTE = "version";
  private static final String ACTIVATION_DATE_ATTRIBUTE = "giltigFrom";

  private final PathMatchingResourcePatternResolver resourceResolver;
  private final EraseCertificatesAggregator eraseCertificatesAggregator;
  private final ExportCertificateAggregator exportCertificateAggregator;

  public List<CertificateTextDTO> getCertificateTexts() {
    try {
      final var resources = resourceResolver.getResources(TEXTS_LOCATION);
      final var textFiles = Arrays.stream(resources).filter(this::isTextFile).toList();
      return getCertificateTexts(textFiles);
    } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
      throw new IllegalStateException("Failed to read certificate texts", e);
    }
  }

  public CertificateExportPageDTO getCertificateExportPage(
      String careProviderId, int collected, int batchSize) {
    return exportCertificateAggregator.exportPage(careProviderId, collected, batchSize);
  }

  public void eraseCertificates(String careProviderId) {
    eraseCertificatesAggregator.eraseCertificates(careProviderId);
  }

  private List<CertificateTextDTO> getCertificateTexts(List<Resource> textFiles)
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
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

  private Document parseTextFile(Resource textFile)
      throws IOException, ParserConfigurationException, SAXException {
    return DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(textFile.getInputStream());
  }

  private boolean isActive(Document textDocument) {
    final var activationDate = getTextAttribute(textDocument, ACTIVATION_DATE_ATTRIBUTE);
    final var activationLocalDate = LocalDate.parse(activationDate);
    return activationLocalDate.isBefore(LocalDate.now())
        || activationLocalDate.isEqual(LocalDate.now());
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
}
