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
package se.inera.intyg.intygstjanst.application.binarycertificate;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.exception.ServerException;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateType;

@Service
@SchemaValidation
public class GetBinaryCertificateResponderImpl implements GetBinaryCertificateResponderInterface {

  @Autowired private BinaryCertificateResponseConverter binaryCertificateResponseConverter;

  @Override
  @PerformanceLogging(
      eventAction = "retrieve-binary-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED,
      isActive = false)
  public GetBinaryCertificateResponseType getBinaryCertificate(
      String logicalAddress, GetBinaryCertificateType getBinaryCertificateType) {
    if (getBinaryCertificateType == null
        || getBinaryCertificateType.getIntygsId() == null
        || getBinaryCertificateType.getIntygsId().getExtension() == null
        || getBinaryCertificateType.getIntygsId().getExtension().isEmpty()
        || logicalAddress == null
        || logicalAddress.isEmpty()) {
      throw new ServerException(
          "Request to GetBinaryCertificate is missing required parameter 'intygs-id'");
    }

    return null;
  }
}
