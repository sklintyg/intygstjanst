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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCareProvider;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCode;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateMetadataDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateUnit;
import se.inera.intyg.intygstjanst.integration.webcert.dto.HosPersonalDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.IntygsStatusDTO;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.BinartIntyg;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.BinaryDataType;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponseType;
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

/**
 * Converts the webcert rest client response ({@link BinaryCertificateResponseDTO}) onto the SOAP
 * response type ({@link GetBinaryCertificateResponseType}).
 */
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
    final BinartIntyg binartIntyg = new BinartIntyg();
    final var metadata = dto.getMetadata();
    binartIntyg.setIntygsId(toIntygId(metadata.getCertificateId()));
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
    List<IntygsStatus> intygsStatuses = new ArrayList<>();
    LocalDateTime signedAt = metadata.getSignedAt();
    LocalDateTime sentAt = metadata.getSentAt();
    LocalDateTime revokedAt = metadata.getRevokedAt();
    IntygsStatus intygsStatusSigned = new IntygsStatus();
    intygsStatusSigned.setTidpunkt(signedAt);
    intygsStatuses.add(intygsStatusSigned);

    if (metadata.getSentAt() != null) {
      IntygsStatus intygsStatusSent = new IntygsStatus();
      intygsStatusSent.setTidpunkt(sentAt);
      intygsStatuses.add(intygsStatusSent);
    }
    if (metadata.getRevokedAt() != null) {
      IntygsStatus intygsStatusRevoked = new IntygsStatus();
      intygsStatusRevoked.setTidpunkt(revokedAt);
      intygsStatuses.add(intygsStatusRevoked);
    }

    return intygsStatuses;
  }

  private IntygId toIntygId(String certificateId) {
    if (certificateId == null) {
      return null;
    }
    final IntygId intygId = new IntygId();
    intygId.setRoot(""); // enhets-hsa?
    intygId.setExtension(certificateId);
    return intygId;
  }

  private TypAvIntyg toTypAvIntyg(BinaryCertificateCode binaryCertificateCode) {
    if (binaryCertificateCode == null) {
      return null;
    }
    final TypAvIntyg typAvIntyg = new TypAvIntyg();
    typAvIntyg.setCode(binaryCertificateCode.getCode());
    typAvIntyg.setCodeSystem(binaryCertificateCode.getCodeSystem());
    typAvIntyg.setDisplayName(binaryCertificateCode.getDisplayName());
    return typAvIntyg;
  }

  private Patient toPatient(se.inera.intyg.common.support.facade.model.Patient patientDTO) {
    if (patientDTO == null) {
      return null;
    }

    final Patient patient = new Patient();
    PersonId personId = new PersonId();
    personId.setRoot(patientDTO.getPersonId().getType());
    personId.setExtension(patientDTO.getPersonId().getId());
    patient.setPersonId(personId);
    patient.setFornamn(patientDTO.getFirstName());
    patient.setMellannamn(patientDTO.getMiddleName());
    patient.setEfternamn(patientDTO.getLastName());
    patient.setPostadress(patientDTO.getStreet());
    patient.setPostnummer(patientDTO.getZipCode());
    patient.setPostort(patientDTO.getCity());
    return patient;
  }

  private HosPersonal toHosPersonal(HosPersonalDTO hosPersonalDTO) {
    if (hosPersonalDTO == null) {
      return null;
    }
    final HosPersonal hosPersonal = new HosPersonal();
    hosPersonal.setPersonalId(toHsaId(hosPersonalDTO.getPersonId()));
    hosPersonal.setFullstandigtNamn(hosPersonalDTO.getFullName());
    hosPersonal.setForskrivarkod("0000000"); // TODO: correct?
    hosPersonal.setEnhet(toEnhet(hosPersonalDTO.getUnit()));
    hosPersonal.getBefattning();
    hosPersonal.getLegitimeratYrke();
    hosPersonal.getSpecialistkompetens();
    return hosPersonal;
  }

  private HsaId toHsaId(String idDTO) {
    if (idDTO == null) {
      return null;
    }
    final HsaId hsaId = new HsaId();
    // hsaId.setRoot(idDTO.getRoot());
    hsaId.setExtension(idDTO);
    return hsaId;
  }

  private Enhet toEnhet(BinaryCertificateUnit binaryCertificateUnit) {
    if (binaryCertificateUnit == null) {
      return null;
    }
    final Enhet enhet = new Enhet();
    enhet.setEnhetsId(toHsaId(binaryCertificateUnit.getUnitId()));
    enhet.setEnhetsnamn(binaryCertificateUnit.getUnitName());
    enhet.setPostadress(binaryCertificateUnit.getAddress());
    enhet.setPostnummer(binaryCertificateUnit.getZipCode());
    enhet.setPostort(binaryCertificateUnit.getCity());
    enhet.setTelefonnummer(binaryCertificateUnit.getPhoneNumber());
    enhet.setEpost(binaryCertificateUnit.getEmail());
    enhet.setVardgivare(toVardgivare(binaryCertificateUnit.getCareProvider()));
    return enhet;
  }

  private Vardgivare toVardgivare(BinaryCertificateCareProvider binaryCertificateCareProvider) {
    if (binaryCertificateCareProvider == null) {
      return null;
    }
    final Vardgivare vardgivare = new Vardgivare();
    vardgivare.setVardgivareId(toHsaId(binaryCertificateCareProvider.getUnitId()));
    vardgivare.setVardgivarnamn(binaryCertificateCareProvider.getUnitName());
    return vardgivare;
  }

  private List<Relation> toRelationList(CertificateRelations relationDTOs) {
    List<Relation> relations = new ArrayList<>();

    for (var relationDTO : relationDTOs.getChildren()) {
      var relation = new Relation();
      IntygId intygId = new IntygId();
      intygId.setExtension(relationDTO.getCertificateId());
      relation.setIntygsId(intygId);
      TypAvRelation typAvRelation = new TypAvRelation();
      typAvRelation.setCode(relationDTO.getType().name());
      relation.setTyp(typAvRelation);
      relations.add(relation);
    }

    return relations;
  }

  private List<IntygsStatus> toIntygsStatusList(List<IntygsStatusDTO> intygsStatusDTOs) {
    return Optional.ofNullable(intygsStatusDTOs).orElseGet(List::of).stream()
        .map(this::toIntygsStatus)
        .toList();
  }

  private IntygsStatus toIntygsStatus(IntygsStatusDTO intygsStatusDTO) {
    final IntygsStatus intygsStatus = new IntygsStatus();
    intygsStatus.setPart(toPart(intygsStatusDTO.getParty()));
    intygsStatus.setStatus(toStatuskod(intygsStatusDTO.getStatus()));
    intygsStatus.setTidpunkt(intygsStatusDTO.getTimestamp());
    return intygsStatus;
  }

  private Part toPart(BinaryCertificateCode binaryCertificateCode) {
    if (binaryCertificateCode == null) {
      return null;
    }
    final Part part = new Part();
    part.setCode(binaryCertificateCode.getCode());
    part.setCodeSystem(binaryCertificateCode.getCodeSystem());
    part.setDisplayName(binaryCertificateCode.getDisplayName());
    return part;
  }

  private Statuskod toStatuskod(BinaryCertificateCode binaryCertificateCode) {
    if (binaryCertificateCode == null) {
      return null;
    }
    final Statuskod statuskod = new Statuskod();
    statuskod.setCode(binaryCertificateCode.getCode());
    statuskod.setCodeSystem(binaryCertificateCode.getCodeSystem());
    statuskod.setDisplayName(binaryCertificateCode.getDisplayName());
    return statuskod;
  }

  private BinaryDataType toBinaryDataType(byte[] data) {
    if (data == null) {
      return null;
    }
    final BinaryDataType binaryDataType = new BinaryDataType();
    binaryDataType.setContentType("application/pdf");
    binaryDataType.setData(data);
    return binaryDataType;
  }
}
