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
package se.inera.intyg.intygstjanst.testability;

import com.google.common.base.Strings;
import com.google.common.io.Resources;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.ApprovalStatusType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.ReceiverApprovalStatus;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.application.certificate.converter.ConverterUtil;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;

/**
 * @author andreaskaltenbach
 */
@RestController
@ApiBasePath("/resources")
@RequestMapping("/certificate")
@Profile({"dev", "testability-api"})
public class CertificateResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateResource.class);

  @PersistenceContext private EntityManager entityManager;

  @Autowired private IntygModuleRegistry moduleRegistry;

  @GetMapping("/{id}")
  public Certificate getCertificate(@PathVariable("id") String id) {
    return entityManager.find(Certificate.class, id);
  }

  @DeleteMapping("/citizen/{id}")
  @Transactional
  public ResponseEntity<?> deleteCertificatesForCitizen(@PathVariable("id") String id) {
    try {
      LOGGER.info("Deleting certificates for citizen {}", id);
      @SuppressWarnings("unchecked")
      List<String> certificates =
          entityManager
              .createQuery(
                  "SELECT c.id FROM Certificate c WHERE c.civicRegistrationNumber=:personId")
              .setParameter("personId", id)
              .getResultList();
      for (String certificate : certificates) {
        removeCertificateById(certificate);
      }
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn("delete certificates for citizen {} failed", id, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "delete certificates for citizen failed", e);
    }
  }

  @DeleteMapping("/unit/{id}")
  @Transactional
  public ResponseEntity<?> deleteCertificatesForUnit(@PathVariable("id") String id) {
    try {
      LOGGER.info("Deleting certificates for unit {}", id);
      @SuppressWarnings("unchecked")
      List<String> certificates =
          entityManager
              .createQuery("SELECT c.id FROM Certificate c WHERE c.careUnitId=:careUnitHsaId")
              .setParameter("careUnitHsaId", id)
              .getResultList();
      for (String certificate : certificates) {
        removeCertificateById(certificate);
      }
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn("delete certificates for unit {} failed", id, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "delete certificates for unit failed", e);
    }
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<?> deleteCertificate(@PathVariable("id") final String id) {
    try {
      LOGGER.info("Deleting certificate {}", id);
      removeCertificateById(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn("delete certificate with id {} failed", id, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "delete certificate failed", e);
    }
  }

  @DeleteMapping()
  @Transactional
  public ResponseEntity<?> deleteAllCertificates() {
    try {
      @SuppressWarnings("unchecked")
      List<Certificate> certificates =
          entityManager.createQuery("SELECT c FROM Certificate c").getResultList();
      for (Certificate certificate : certificates) {
        if (certificate.getOriginalCertificate() != null) {
          entityManager.remove(certificate.getOriginalCertificate());
        }
        entityManager.remove(certificate);
      }

      List<SjukfallCertificate> sjukfallCertificates =
          entityManager
              .createQuery("SELECT c FROM SjukfallCertificate c", SjukfallCertificate.class)
              .getResultList();
      for (SjukfallCertificate sjukfallCert : sjukfallCertificates) {
        entityManager.remove(sjukfallCert);
      }

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn("delete all certificates failed", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "delete all certificates failed", e);
    }
  }

  @PostMapping()
  @Transactional
  public ResponseEntity<?> insertCertificate(
      @RequestBody final CertificateHolder certificateHolder) {
    Certificate certificate = ConverterUtil.toCertificate(certificateHolder);
    try {
      LOGGER.info("insert certificate {} ({})", certificate.getId(), certificate.getType());
      OriginalCertificate originalCertificate =
          new OriginalCertificate(LocalDateTime.now(), getXmlBody(certificateHolder), certificate);
      ModuleApi moduleApi =
          moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
      final Utlatande utlatande = moduleApi.getUtlatandeFromXml(originalCertificate.getDocument());
      certificate.setAdditionalInfo(
          moduleApi.getAdditionalInfo(moduleApi.getIntygFromUtlatande(utlatande)));
      CertificateMetaData metaData =
          new CertificateMetaData(
              certificate,
              utlatande.getGrundData().getSkapadAv().getPersonId(),
              utlatande.getGrundData().getSkapadAv().getFullstandigtNamn(),
              certificate.isRevoked(),
              ConverterUtil.getDiagnoses(certificateHolder.getAdditionalMetaData()));
      certificate.setCertificateMetaData(metaData);
      entityManager.persist(certificate);
      entityManager.persist(metaData);
      entityManager.persist(originalCertificate);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn(
          "insert certificate {} ({}) failed", certificate.getId(), certificate.getType(), e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "insert certificate failed", e);
    }
  }

  @DeleteMapping("/{id}/approvedreceivers")
  @Transactional
  public ResponseEntity<?> deleteApprovedReceivers(@PathVariable("id") final String id) {
    try {
      LOGGER.info("removing approved receivers for certificates {}", id);

      @SuppressWarnings("unchecked")
      List<ApprovedReceiver> approvedReceivers =
          entityManager
              .createQuery(
                  "SELECT ar FROM ApprovedReceiver ar WHERE ar.certificateId=:certificateId")
              .setParameter("certificateId", id)
              .getResultList();

      for (ApprovedReceiver ar : approvedReceivers) {
        entityManager.remove(ar);
      }

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn("removal of approved receivers for certificate {} failed", id, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "delete approved receivers failed", e);
    }
  }

  @PostMapping("/{id}/approvedreceivers")
  @Transactional
  public ResponseEntity<?> registerApprovedReceivers(
      @PathVariable("id") final String id,
      @RequestBody final RegisterApprovedReceiversType registerApprovedReceiversType) {

    String receiverId = null;
    try {
      for (ReceiverApprovalStatus ras : registerApprovedReceiversType.getApprovedReceivers()) {
        receiverId = ras.getReceiverId();
        boolean approvalStatus = parseApprovalStatus(ras.getApprovalStatus().value());

        ApprovedReceiver approvedReceiver = new ApprovedReceiver();
        approvedReceiver.setCertificateId(id);
        approvedReceiver.setReceiverId(receiverId);
        approvedReceiver.setApproved(approvalStatus);

        LOGGER.info("register approved receiver {} for certificate {}", id, receiverId);
        entityManager.persist(approvedReceiver);
      }
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      LOGGER.warn("register approved receiver {} failed for certificate {}", id, receiverId, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "register approved receivers failed", e);
    }
  }

  @DeleteMapping("/deleteCertificates")
  @Transactional
  public ResponseEntity<?> deleteCertificates(@RequestBody List<String> certificateIds) {

    LOGGER.info("Removing data for certificates {}", certificateIds);

    delete(getMessages(certificateIds));
    delete(getApprovedReceivers(certificateIds));
    delete(getRelations(certificateIds));
    delete(getSjukfallCertificates(certificateIds));
    delete(getCertificates(certificateIds));

    return ResponseEntity.ok().build();
  }

  @GetMapping("/{careProviderId}/certificateCount")
  public int getCertificateCountForCareProvider(
      @PathVariable("careProviderId") final String careProviderId) {
    return getCertificateIdsForCareProvider(careProviderId).size();
  }

  @GetMapping("/{careProviderId}/sjukfallCertificateCount")
  public int getSjukfallCertificateCountForCareProvider(
      @PathVariable("careProviderId") final String careProviderId) {
    final var certificateIds = getCertificateIdsForCareProvider(careProviderId);
    return getSjukfallCertificates(certificateIds).size();
  }

  @GetMapping("/{careProviderId}/approvedReceiverCount")
  public int getApprovedReceiversCountForCareProvider(
      @PathVariable("careProviderId") final String careProviderId) {
    final var certificateIds = getCertificateIdsForCareProvider(careProviderId);
    return getApprovedReceivers(certificateIds).size();
  }

  @GetMapping("/{careProviderId}/messageCount")
  public int getMessagesCountForCareProvider(
      @PathVariable("careProviderId") final String careProviderId) {
    final var certificateIds = getCertificateIdsForCareProvider(careProviderId);
    return getMessages(certificateIds).size();
  }

  @GetMapping("/{careProviderId}/relationCount")
  public int getRelationCountForCareProvider(
      @PathVariable("careProviderId") final String careProviderId) {
    final var certificateIds = getCertificateIdsForCareProvider(careProviderId);
    return getRelations(certificateIds).size();
  }

  private void removeCertificateById(String id) {
    Certificate certificate = entityManager.find(Certificate.class, id);
    if (certificate != null) {
      entityManager.remove(certificate.getOriginalCertificate());
      entityManager.remove(certificate);
    }
    SjukfallCertificate sjukfallCertificate = entityManager.find(SjukfallCertificate.class, id);
    if (sjukfallCertificate != null) {
      entityManager.remove(sjukfallCertificate);
    }
  }

  private boolean parseApprovalStatus(String approvalStatus) {
    return approvalStatus != null && approvalStatus.equals(ApprovalStatusType.YES.value());
  }

  private String getXmlBody(CertificateHolder certificateHolder) throws IOException {
    if (!Strings.nullToEmpty(certificateHolder.getOriginalCertificate()).trim().isEmpty()) {
      return certificateHolder.getOriginalCertificate();
    } else {
      return Resources.toString(
              new ClassPathResource("content/intyg-" + certificateHolder.getType() + "-content.xml")
                  .getURL(),
              StandardCharsets.UTF_8)
          .replace("CERTIFICATE_ID", certificateHolder.getId())
          .replace("PATIENT_CRN", certificateHolder.getCivicRegistrationNumber().getPersonnummer())
          .replace("CAREUNIT_ID", certificateHolder.getCareUnitId())
          .replace("CAREUNIT_NAME", certificateHolder.getCareUnitName())
          .replace("CAREGIVER_ID", certificateHolder.getCareGiverId())
          .replace("DOCTOR_NAME", certificateHolder.getSigningDoctorName())
          .replace(
              "SIGNED_DATE",
              certificateHolder
                  .getSignedDate()
                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    }
  }

  private List<String> getCertificateIdsForCareProvider(String careProviderId) {
    return entityManager
        .createQuery(
            "Select c.id FROM Certificate c WHERE c.careGiverId = :careProviderId", String.class)
        .setParameter("careProviderId", careProviderId)
        .getResultList();
  }

  private List<Certificate> getCertificates(List<String> certificateIds) {
    return runQuery(
        "Select c FROM Certificate c WHERE c.id in :certificateIds",
        certificateIds,
        Certificate.class);
  }

  private List<SjukfallCertificate> getSjukfallCertificates(List<String> certificateIds) {
    return runQuery(
        "Select sc FROM SjukfallCertificate sc WHERE sc.id in :certificateIds",
        certificateIds,
        SjukfallCertificate.class);
  }

  private List<Arende> getMessages(List<String> certificateIds) {
    return runQuery(
        "SELECT a FROM Arende a WHERE a.intygsId in :certificateIds", certificateIds, Arende.class);
  }

  private List<Relation> getRelations(List<String> certificateIds) {
    return runQuery(
        "SELECT r FROM Relation r WHERE r.fromIntygsId in :certificateIds",
        certificateIds,
        Relation.class);
  }

  private List<ApprovedReceiver> getApprovedReceivers(List<String> certificateIds) {
    return runQuery(
        "SELECT ar FROM ApprovedReceiver ar WHERE ar.certificateId in :certificateIds",
        certificateIds,
        ApprovedReceiver.class);
  }

  private <T> List<T> runQuery(String query, List<String> certificateIds, Class<T> clazz) {
    if (certificateIds.isEmpty()) {
      return List.of();
    }
    return entityManager
        .createQuery(query, clazz)
        .setParameter("certificateIds", certificateIds)
        .getResultList();
  }

  private <T> void delete(List<T> items) {
    for (T item : items) {
      entityManager.remove(item);
    }
  }
}
