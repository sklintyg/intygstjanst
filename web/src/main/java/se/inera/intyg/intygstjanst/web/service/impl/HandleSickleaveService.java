package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;

@Service
@RequiredArgsConstructor
public class HandleSickleaveService {

  private static final String FK7804_TYPE = "fk7804";
  private static final String QUESTION_SMITTBARARPENNING_ID = "27";

  private final SjukfallCertificateDao sjukfallCertificateDao;
  private final CSIntegrationService csIntegrationService;

  public void created(GetCertificateXmlResponse response) {
    if (!FK7804_TYPE.equals(response.getCertificateType())) {
      return;
    }

    final var certificate = csIntegrationService.getCertificate(response.getCertificateId());

    final var questionSmittbararpenning = certificate.getData().entrySet().stream()
        .filter(e -> QUESTION_SMITTBARARPENNING_ID.equals(e.getKey()))
        .findFirst()
        .map(Entry::getValue)
        .map(CertificateDataElement::getValue)
        .map(CertificateDataValueBoolean.class::cast)
        .map(CertificateDataValueBoolean::getSelected)
        .orElseThrow(() -> new IllegalStateException("Could not find data element %s in certificate".formatted(QUESTION_SMITTBARARPENNING_ID)));

    if (questionSmittbararpenning) {
      return;
    }



    sjukfallCertificateDao.store(new SjukfallCertificate("id"));

  }
}
