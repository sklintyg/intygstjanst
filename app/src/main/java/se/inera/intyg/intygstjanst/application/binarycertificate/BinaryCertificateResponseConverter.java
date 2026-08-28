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

import static se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePersonIdType.PERSONAL_IDENTITY_NUMBER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.Constants;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCareProvider;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCode;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateMetadataDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePatient;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateStaff;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateUnit;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.BinartIntyg;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.BinaryDataType;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
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
    binartIntyg.getRelation().addAll(toRelationList(metadata.getRelations()));
    binartIntyg.getStatus().addAll(addStatuses(metadata));
    binartIntyg.setBinartSvar(toBinaryDataType(dto.getPdfData()));
    return binartIntyg;
  }

  private List<IntygsStatus> addStatuses(BinaryCertificateMetadataDTO metadata) {
    final var intygStatuses = new ArrayList<IntygsStatus>();
    final var signedAt = metadata.getSignedAt();
    final var sentAt = metadata.getSentAt();
    final var revokedAt = metadata.getRevokedAt();

    intygStatuses.add(toIntygsStatus("HSVARD", CertificateState.RECEIVED, signedAt));

    if (metadata.getSentAt() != null) {
      intygStatuses.add(toIntygsStatus("TO BE FIXED", CertificateState.SENT, sentAt));
    }
    if (metadata.getRevokedAt() != null) {
      intygStatuses.add(toIntygsStatus("HSVARD", CertificateState.CANCELLED, revokedAt));
    }

    return intygStatuses;
  }

  private IntygsStatus toIntygsStatus(String partCode, CertificateState state,
      LocalDateTime timestamp) {
    final var part = new Part();
    part.setCode(partCode);
    part.setCodeSystem(Constants.KV_PART_CODE_SYSTEM);
    final var intygsStatus = new IntygsStatus();
    intygsStatus.setPart(part);
    intygsStatus.setStatus(getStatuskod(state));
    intygsStatus.setTidpunkt(timestamp);
    return intygsStatus;
  }

  private static Statuskod getStatuskod(CertificateState state) {
    final var statusCode = new Statuskod();
    statusCode.setCode(state.name());
    statusCode.setCodeSystem(Constants.KV_STATUS_CODE_SYSTEM);
    return statusCode;
  }

  private IntygId toIntygId(String certificateId, BinaryCertificateStaff staff) {
    if (certificateId == null) {
      return null;
    }
    final var intygId = new IntygId();
    intygId.setExtension(certificateId);
    intygId.setRoot(staff.getUnit().getUnitId()); //TODO Will this be coreect?
    return intygId;
  }

  private TypAvIntyg toTypAvIntyg(BinaryCertificateCode binaryCertificateCode) {
    if (binaryCertificateCode == null) {
      return null;
    }
    final var typAvIntyg = new TypAvIntyg();
    typAvIntyg.setCode(binaryCertificateCode.getCode());
    typAvIntyg.setCodeSystem(binaryCertificateCode.getCodeSystem());
    typAvIntyg.setDisplayName(binaryCertificateCode.getDisplayName());
    return typAvIntyg;
  }

  private Patient toPatient(BinaryCertificatePatient patientDTO) {
    if (patientDTO == null) {
      return null;
    }

    final var personId = new PersonId();
    final var patient = new Patient();
    personId.setExtension(patientDTO.getPatientId());
    personId.setRoot(patientDTO.getType() == PERSONAL_IDENTITY_NUMBER
        ? Constants.PERSON_ID_OID
        : Constants.SAMORDNING_ID_OID);
    patient.setPersonId(personId);
    patient.setFornamn("");
    patient.setEfternamn("");
    patient.setPostadress("");
    patient.setPostnummer("");
    patient.setPostort("");
    return patient;
  }

  private HosPersonal toHosPersonal(BinaryCertificateStaff staff) {
    if (staff == null) {
      return null;
    }
    final var hosPersonal = new HosPersonal();
    hosPersonal.setPersonalId(toHsaId(staff.getPersonId()));
    hosPersonal.setFullstandigtNamn(staff.getFullName());
    hosPersonal.setForskrivarkod("0000000"); // TODO: correct?
    hosPersonal.setEnhet(toEnhet(staff.getUnit()));
    hosPersonal.getBefattning();
    hosPersonal.getLegitimeratYrke();
    hosPersonal.getSpecialistkompetens();
    return hosPersonal;
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

  private List<Relation> toRelationList(CertificateRelations relationDTOs) {
    final var relations = new ArrayList<Relation>();

    for (var relationDTO : relationDTOs.getChildren()) {
      final var intygId = new IntygId();
      final var typAvRelation = new TypAvRelation();
      final var relation = new Relation();
      intygId.setExtension(relationDTO.getCertificateId());
      typAvRelation.setCode(relationDTO.getType().name());
      relation.setIntygsId(intygId);
      relation.setTyp(typAvRelation);
      relations.add(relation);
    }

    return relations;
  }

  private BinaryDataType toBinaryDataType(byte[] data) {
    if (data == null) {
      return null;
    }
    final var binaryDataType = new BinaryDataType();
    binaryDataType.setContentType("application/pdf");
    binaryDataType.setData(data);
    return binaryDataType;
  }
}
