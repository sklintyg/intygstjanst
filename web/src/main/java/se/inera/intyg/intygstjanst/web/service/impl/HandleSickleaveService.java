package se.inera.intyg.intygstjanst.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;

@Service
@RequiredArgsConstructor
public class HandleSickleaveService {

  private static final String FK7804_TYPE = "fk7804";

  private final SjukfallCertificateDao sjukfallCertificateDao;
  private final CSIntegrationService csIntegrationService;

  public void created(GetCertificateXmlResponse response, String certificateXml) {
    if (!FK7804_TYPE.equals(response.getCertificateType())) {
      return;
    }

    final var certificate = csIntegrationService.getCertificate(response.getCertificateId());



  }
}
