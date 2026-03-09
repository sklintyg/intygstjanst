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
package se.inera.intyg.intygstjanst.application.certificate;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.application.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.intygstjanst.application.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.intygstjanst.application.certificate.dto.TypedCertificateRequest;
import se.inera.intyg.intygstjanst.application.certificate.service.TypedCertificateService;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.schemas.contract.Personnummer;

/** Internal REST endpoint to retrieve certificates */
@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/typedcertificate")
@RequiredArgsConstructor
public class TypedCertificateController {

  private final TypedCertificateService typedCertificateService;

  @PostMapping("/diagnosed/unit")
  @PerformanceLogging(
      eventAction = "list-certificates",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
  public List<DiagnosedCertificate> listDiagnosedCertificatesForCareUnit(
      @RequestBody TypedCertificateRequest parameters) {
    var units = parameters.getUnitIds();

    if (units == null || units.isEmpty()) {
      return Collections.emptyList();
    }

    return typedCertificateService.listDiagnosedCertificatesForCareUnits(
        units,
        parameters.getCertificateTypes(),
        parameters.getFromDate(),
        parameters.getToDate(),
        parameters.getDoctorIds());
  }

  @PostMapping("/doctors")
  @PerformanceLogging(eventAction = "list-doctors", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
  public List<String> listDoctorsForCareUnit(@RequestBody TypedCertificateRequest parameters) {
    var units = parameters.getUnitIds();

    if (units == null || units.isEmpty()) {
      return Collections.emptyList();
    }

    return typedCertificateService.listDoctorsForCareUnits(
        units, parameters.getCertificateTypes(), parameters.getFromDate(), parameters.getToDate());
  }

  @PostMapping("/diagnosed/person")
  @PerformanceLogging(
      eventAction = "list-certificates",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
  public List<DiagnosedCertificate> listDiagnosedCertificatesForCitizen(
      @RequestBody TypedCertificateRequest parameters) {
    var optionalPersonnummer = Personnummer.createPersonnummer(parameters.getPersonId());

    if (optionalPersonnummer.isEmpty()) {
      return Collections.emptyList();
    }

    return typedCertificateService.listDiagnosedCertificatesForPerson(
        optionalPersonnummer.get(),
        parameters.getCertificateTypes(),
        parameters.getFromDate(),
        parameters.getToDate(),
        parameters.getUnitIds());
  }

  @PostMapping("/sickleave/person")
  @PerformanceLogging(
      eventAction = "list-certificates",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
  public List<SickLeaveCertificate> listSickLeaveCertificatesForCitizen(
      @RequestBody TypedCertificateRequest parameters) {
    var optionalPersonnummer = Personnummer.createPersonnummer(parameters.getPersonId());

    if (optionalPersonnummer.isEmpty()) {
      return Collections.emptyList();
    }

    return typedCertificateService.listSickLeaveCertificatesForPerson(
        optionalPersonnummer.get(),
        parameters.getCertificateTypes(),
        parameters.getFromDate(),
        parameters.getToDate(),
        parameters.getUnitIds(),
        parameters.getDoctorIds());
  }
}
