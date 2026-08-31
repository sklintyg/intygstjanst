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

import static se.inera.intyg.common.support.Constants.KV_RELATION_CODE_SYSTEM;
import static se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePersonIdType.PERSONAL_IDENTITY_NUMBER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.Constants;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCareProvider;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCode;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateMetadataDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePatient;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateRelation;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateStaff;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateUnit;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.BinartIntyg;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.BinaryDataType;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Befattning;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.CVType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.LegitimeratYrkeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Specialistkompetens;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvRelation;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.IntygsStatus;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.Relation;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

@Component
public class BinaryCertificateResponseConverter {

  private static final String PART_HSVARD = "HSVARD";
  private static final String MIME_TYPE_APPLICATION_PDF = "application/pdf";
  private static final String NOT_AVAILABLE = "N/A";
  public static final String EMPTY_STRING = "";

  public GetBinaryCertificateResponseType toResponse(BinaryCertificateResponseDTO dto) {
    if (dto == null) {
      return null;
    }

    final GetBinaryCertificateResponseType response = new GetBinaryCertificateResponseType();
    response.setBinartIntyg(toBinartIntyg(dto));
    return response;
  }

  private BinartIntyg toBinartIntyg(BinaryCertificateResponseDTO dto) {
    final var binartIntyg = new BinartIntyg();
    final var metadata = dto.getMetadata();
    binartIntyg.setIntygsId(toIntygId(metadata.getCertificateId(), metadata.getIssuedBy()));
    binartIntyg.setTyp(toTypAvIntyg(metadata.getType()));
    binartIntyg.setVersion(metadata.getVersion());
    binartIntyg.setSigneringstidpunkt(metadata.getSignedAt());
    binartIntyg.setPatient(toPatient(metadata.getPatient()));
    binartIntyg.setSkapadAv(toHosPersonal(metadata.getIssuedBy()));
    binartIntyg.getRelation().add(toRelation(metadata.getParentRelation()));
    binartIntyg.getStatus().addAll(toStatuses(metadata));
    binartIntyg.setBinartSvar(toBinaryDataType(dto.getPdfData()));
    return binartIntyg;
  }

  private IntygId toIntygId(String certificateId, BinaryCertificateStaff staff) {
    if (certificateId == null) {
      return null;
    }
    final var intygId = new IntygId();
    intygId.setExtension(certificateId);
    intygId.setRoot(staff.getUnit().getUnitId());
    return intygId;
  }

  private TypAvIntyg toTypAvIntyg(BinaryCertificateCode binaryCertificateCode) {
    return binaryCertificateCode == null ? null : toCode(binaryCertificateCode, TypAvIntyg::new);
  }

  private Patient toPatient(BinaryCertificatePatient patientDTO) {
    if (patientDTO == null) {
      return null;
    }

    final var personId = new PersonId();
    final var patient = new Patient();
    personId.setExtension(patientDTO.getPatientId());
    personId.setRoot(
        patientDTO.getType() == PERSONAL_IDENTITY_NUMBER
            ? Constants.PERSON_ID_OID
            : Constants.SAMORDNING_ID_OID);
    patient.setPersonId(personId);
    patient.setFornamn(EMPTY_STRING);
    patient.setEfternamn(EMPTY_STRING);
    patient.setPostadress(EMPTY_STRING);
    patient.setPostnummer(EMPTY_STRING);
    patient.setPostort(EMPTY_STRING);
    return patient;
  }

  private HosPersonal toHosPersonal(BinaryCertificateStaff staff) {
    if (staff == null) {
      return null;
    }
    final var hosPersonal = new HosPersonal();
    hosPersonal.setPersonalId(toHsaId(staff.getPersonId()));
    hosPersonal.setFullstandigtNamn(staff.getFullName());
    hosPersonal.setForskrivarkod("0000000");
    hosPersonal.setEnhet(toEnhet(staff.getUnit()));
    hosPersonal.getBefattning().addAll(toCodes(staff.getTitles(), Befattning::new));
    hosPersonal.getLegitimeratYrke().addAll(toCodes(staff.getLicences(), LegitimeratYrkeType::new));
    hosPersonal.getSpecialistkompetens().addAll(toSpecialities(staff.getSpecialities()));
    return hosPersonal;
  }

  private Relation toRelation(BinaryCertificateRelation parentRelation) {
    if (parentRelation == null) {
      return null;
    }

    final var relationKod = toRelationKod(parentRelation.getType());
    final var intygId = new IntygId();
    final var typAvRelation = new TypAvRelation();
    final var relation = new Relation();
    intygId.setExtension(parentRelation.getCertificateId());
    intygId.setRoot(parentRelation.getIssuingUnitId());
    typAvRelation.setCode(relationKod.value());
    typAvRelation.setCodeSystem(KV_RELATION_CODE_SYSTEM);
    typAvRelation.setDisplayName(relationKod.getKlartext());
    relation.setIntygsId(intygId);
    relation.setTyp(typAvRelation);
    return relation;
  }

  private List<IntygsStatus> toStatuses(BinaryCertificateMetadataDTO metadata) {
    final var intygStatuses = new ArrayList<IntygsStatus>();
    final var signedAt = metadata.getSignedAt();
    final var sentAt = metadata.getSentAt();
    final var revokedAt = metadata.getRevokedAt();
    final var recipientId = metadata.getRecipientId();

    intygStatuses.add(toIntygsStatus(PART_HSVARD, CertificateState.RECEIVED, signedAt));

    if (metadata.getSentAt() != null) {
      intygStatuses.add(toIntygsStatus(recipientId, CertificateState.SENT, sentAt));
    }
    if (metadata.getRevokedAt() != null) {
      intygStatuses.add(toIntygsStatus(PART_HSVARD, CertificateState.CANCELLED, revokedAt));
    }

    return intygStatuses;
  }

  private BinaryDataType toBinaryDataType(byte[] data) {
    if (data == null) {
      return null;
    }
    final var binaryDataType = new BinaryDataType();
    binaryDataType.setContentType(MIME_TYPE_APPLICATION_PDF);
    binaryDataType.setData(data);
    return binaryDataType;
  }

  private HsaId toHsaId(String idDTO) {
    if (idDTO == null) {
      return null;
    }
    final var hsaId = new HsaId();
    hsaId.setRoot(Constants.HSA_ID_OID);
    hsaId.setExtension(idDTO);
    return hsaId;
  }

  private Enhet toEnhet(BinaryCertificateUnit unit) {
    if (unit == null) {
      return null;
    }
    final var enhet = new Enhet();
    final var workplaceCode = new ArbetsplatsKod();
    workplaceCode.setExtension(unit.getWorkplaceCode());
    workplaceCode.setRoot(Constants.ARBETSPLATS_KOD_OID);
    enhet.setEnhetsId(toHsaId(unit.getUnitId()));
    enhet.setEnhetsnamn(unit.getUnitName());
    enhet.setPostadress(unit.getAddress());
    enhet.setPostnummer(unit.getZipCode());
    enhet.setPostort(unit.getCity());
    enhet.setTelefonnummer(unit.getPhoneNumber());
    enhet.setEpost(unit.getEmail());
    enhet.setVardgivare(toVardgivare(unit.getCareProvider()));
    enhet.setArbetsplatskod(workplaceCode);
    return enhet;
  }

  private Vardgivare toVardgivare(BinaryCertificateCareProvider binaryCertificateCareProvider) {
    if (binaryCertificateCareProvider == null) {
      return null;
    }
    final var vardgivare = new Vardgivare();
    vardgivare.setVardgivareId(toHsaId(binaryCertificateCareProvider.getUnitId()));
    vardgivare.setVardgivarnamn(binaryCertificateCareProvider.getUnitName());
    return vardgivare;
  }

  private List<Specialistkompetens> toSpecialities(List<String> specialities) {
    if (specialities == null) {
      return Collections.emptyList();
    }

    return specialities.stream().map(this::toSpeciality).toList();
  }

  private Specialistkompetens toSpeciality(String spec) {
    final var specialistkompetens = new Specialistkompetens();
    specialistkompetens.setCode(NOT_AVAILABLE);
    specialistkompetens.setDisplayName(spec);
    return specialistkompetens;
  }

  private RelationKod toRelationKod(CertificateRelationType type) {
    return switch (type) {
      case REPLACED -> RelationKod.ERSATT;
      case COPIED -> RelationKod.KOPIA;
      case EXTENDED -> RelationKod.FRLANG;
      case COMPLEMENTED -> RelationKod.KOMPLT;
    };
  }

  private IntygsStatus toIntygsStatus(
      String partCode, CertificateState state, LocalDateTime timestamp) {
    final var intygsStatus = new IntygsStatus();
    final var part = new Part();
    part.setCode(partCode);
    part.setCodeSystem(Constants.KV_PART_CODE_SYSTEM);
    intygsStatus.setPart(part);
    intygsStatus.setStatus(toStatuskod(state));
    intygsStatus.setTidpunkt(timestamp);
    return intygsStatus;
  }

  private static Statuskod toStatuskod(CertificateState state) {
    final var statusCode = new Statuskod();
    statusCode.setCode(state.name());
    statusCode.setCodeSystem(Constants.KV_STATUS_CODE_SYSTEM);
    return statusCode;
  }

  private <T extends CVType> List<T> toCodes(List<BinaryCertificateCode> codes,
      Supplier<T> factory) {
    if (codes == null) {
      return Collections.emptyList();
    }
    return codes.stream().map(code -> toCode(code, factory)).toList();
  }

  private <T extends CVType> T toCode(BinaryCertificateCode code, Supplier<T> factory) {
    final var t = factory.get();
    t.setCode(code.getCode());
    t.setCodeSystem(code.getCodeSystem());
    t.setDisplayName(code.getDisplayName());
    return t;
  }
}
