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

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryDataDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.CodeableConceptDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.EnhetDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.HosPersonalDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.IdDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.IntygsStatusDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.PatientDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.RelationDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.VardgivareDTO;
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
    binartIntyg.setIntygsId(toIntygId(dto.getCertificateId()));
    binartIntyg.setTyp(toTypAvIntyg(dto.getType()));
    binartIntyg.setVersion(dto.getVersion());
    binartIntyg.setSigneringstidpunkt(dto.getSigningTimestamp());
    binartIntyg.setPatient(toPatient(dto.getPatient()));
    binartIntyg.setSkapadAv(toHosPersonal(dto.getCreatedBy()));
    binartIntyg.getRelation().addAll(toRelationList(dto.getRelations()));
    binartIntyg.getStatus().addAll(toIntygsStatusList(dto.getStatuses()));
    binartIntyg.setBinartSvar(toBinaryDataType(dto.getBinaryData()));
    return binartIntyg;
  }

  private IntygId toIntygId(IdDTO idDTO) {
    if (idDTO == null) {
      return null;
    }
    final IntygId intygId = new IntygId();
    intygId.setRoot(idDTO.getRoot());
    intygId.setExtension(idDTO.getExtension());
    return intygId;
  }

  private TypAvIntyg toTypAvIntyg(CodeableConceptDTO codeableConceptDTO) {
    if (codeableConceptDTO == null) {
      return null;
    }
    final TypAvIntyg typAvIntyg = new TypAvIntyg();
    typAvIntyg.setCode(codeableConceptDTO.getCode());
    typAvIntyg.setCodeSystem(codeableConceptDTO.getCodeSystem());
    typAvIntyg.setDisplayName(codeableConceptDTO.getDisplayName());
    return typAvIntyg;
  }

  private Patient toPatient(PatientDTO patientDTO) {
    if (patientDTO == null) {
      return null;
    }
    final Patient patient = new Patient();
    patient.setPersonId(toPersonId(patientDTO.getPersonId()));
    patient.setFornamn(patientDTO.getFirstName());
    patient.setMellannamn(patientDTO.getMiddleName());
    patient.setEfternamn(patientDTO.getLastName());
    patient.setPostadress(patientDTO.getAddress());
    patient.setPostnummer(patientDTO.getPostalCode());
    patient.setPostort(patientDTO.getCity());
    return patient;
  }

  private PersonId toPersonId(IdDTO idDTO) {
    if (idDTO == null) {
      return null;
    }
    final PersonId personId = new PersonId();
    personId.setRoot(idDTO.getRoot());
    personId.setExtension(idDTO.getExtension());
    return personId;
  }

  private HosPersonal toHosPersonal(HosPersonalDTO hosPersonalDTO) {
    if (hosPersonalDTO == null) {
      return null;
    }
    final HosPersonal hosPersonal = new HosPersonal();
    hosPersonal.setPersonalId(toHsaId(hosPersonalDTO.getStaffId()));
    hosPersonal.setFullstandigtNamn(hosPersonalDTO.getFullName());
    hosPersonal.setForskrivarkod(hosPersonalDTO.getPrescriberCode());
    hosPersonal.setEnhet(toEnhet(hosPersonalDTO.getUnit()));
    return hosPersonal;
  }

  private HsaId toHsaId(IdDTO idDTO) {
    if (idDTO == null) {
      return null;
    }
    final HsaId hsaId = new HsaId();
    hsaId.setRoot(idDTO.getRoot());
    hsaId.setExtension(idDTO.getExtension());
    return hsaId;
  }

  private Enhet toEnhet(EnhetDTO enhetDTO) {
    if (enhetDTO == null) {
      return null;
    }
    final Enhet enhet = new Enhet();
    enhet.setEnhetsId(toHsaId(enhetDTO.getUnitId()));
    enhet.setEnhetsnamn(enhetDTO.getUnitName());
    enhet.setPostadress(enhetDTO.getAddress());
    enhet.setPostnummer(enhetDTO.getPostalCode());
    enhet.setPostort(enhetDTO.getCity());
    enhet.setTelefonnummer(enhetDTO.getPhoneNumber());
    enhet.setEpost(enhetDTO.getEmail());
    enhet.setVardgivare(toVardgivare(enhetDTO.getCareProvider()));
    return enhet;
  }

  private Vardgivare toVardgivare(VardgivareDTO vardgivareDTO) {
    if (vardgivareDTO == null) {
      return null;
    }
    final Vardgivare vardgivare = new Vardgivare();
    vardgivare.setVardgivareId(toHsaId(vardgivareDTO.getCareProviderId()));
    vardgivare.setVardgivarnamn(vardgivareDTO.getCareProviderName());
    return vardgivare;
  }

  private List<Relation> toRelationList(List<RelationDTO> relationDTOs) {
    return Optional.ofNullable(relationDTOs).orElseGet(List::of).stream()
        .map(this::toRelation)
        .toList();
  }

  private Relation toRelation(RelationDTO relationDTO) {
    final Relation relation = new Relation();
    relation.setTyp(toTypAvRelation(relationDTO.getType()));
    relation.setIntygsId(toIntygId(relationDTO.getCertificateId()));
    return relation;
  }

  private TypAvRelation toTypAvRelation(CodeableConceptDTO codeableConceptDTO) {
    if (codeableConceptDTO == null) {
      return null;
    }
    final TypAvRelation typAvRelation = new TypAvRelation();
    typAvRelation.setCode(codeableConceptDTO.getCode());
    typAvRelation.setCodeSystem(codeableConceptDTO.getCodeSystem());
    typAvRelation.setDisplayName(codeableConceptDTO.getDisplayName());
    return typAvRelation;
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

  private Part toPart(CodeableConceptDTO codeableConceptDTO) {
    if (codeableConceptDTO == null) {
      return null;
    }
    final Part part = new Part();
    part.setCode(codeableConceptDTO.getCode());
    part.setCodeSystem(codeableConceptDTO.getCodeSystem());
    part.setDisplayName(codeableConceptDTO.getDisplayName());
    return part;
  }

  private Statuskod toStatuskod(CodeableConceptDTO codeableConceptDTO) {
    if (codeableConceptDTO == null) {
      return null;
    }
    final Statuskod statuskod = new Statuskod();
    statuskod.setCode(codeableConceptDTO.getCode());
    statuskod.setCodeSystem(codeableConceptDTO.getCodeSystem());
    statuskod.setDisplayName(codeableConceptDTO.getDisplayName());
    return statuskod;
  }

  private BinaryDataType toBinaryDataType(BinaryDataDTO binaryDataDTO) {
    if (binaryDataDTO == null) {
      return null;
    }
    final BinaryDataType binaryDataType = new BinaryDataType();
    binaryDataType.setContentType(binaryDataDTO.getContentType());
    binaryDataType.setData(binaryDataDTO.getData());
    return binaryDataType;
  }
}
