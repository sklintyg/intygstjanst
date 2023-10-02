package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.GetCitizenCertificateRecipientService;
import se.inera.intyg.intygstjanst.web.service.GetCitizenCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.GetCitizenCertificateRecipientRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.GetCitizenCertificateRecipientResponseDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.GetCitizenCertificateRequestDTO;

@Service
public class GetCitizenCertificateRecipientServiceImpl implements
    GetCitizenCertificateRecipientService {

  private final GetCitizenCertificateService getCitizenCertificateService;

  public GetCitizenCertificateRecipientServiceImpl(GetCitizenCertificateService getCitizenCertificateService) {
    this.getCitizenCertificateService = getCitizenCertificateService;
  }

  @Override
  public GetCitizenCertificateRecipientResponseDTO get(
      GetCitizenCertificateRecipientRequestDTO request) {
    final var certificateResponse = getCitizenCertificateService.get(
        GetCitizenCertificateRequestDTO
            .builder()
            .certificateId(request.getCertificateId())
            .build()
    );

    return GetCitizenCertificateRecipientResponseDTO
        .builder()
        .recipient(
            certificateResponse
                .getCertificate()
                .getRecipient()
        )
        .build();
  }
}
