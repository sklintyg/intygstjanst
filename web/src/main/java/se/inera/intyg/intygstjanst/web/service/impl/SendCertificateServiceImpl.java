package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;

@Service
public class SendCertificateServiceImpl implements SendCertificateService {
  private final CertificateService certificateService;
  private final StatisticsService statisticsService;
  private final InternalNotificationService internalNotificationService;

  public SendCertificateServiceImpl(
      CertificateService certificateService, StatisticsService statisticsService,
      InternalNotificationService internalNotificationService) {
    this.certificateService = certificateService;
    this.statisticsService = statisticsService;
    this.internalNotificationService = internalNotificationService;
  }

  // TODO: Should we add some logging here? Thinking of the "Certificate sent" "Certificate already sent" in SendCertificateToRecipientResponderImpl


  @Override
  public SendStatus send(SendCertificateRequestDTO request)
      throws InvalidCertificateException, TestCertificateException, CertificateRevokedException, RecipientUnknownException {

    final var certificate = certificateService.getCertificateForCare(request.getCertificateId());
    final var sendStatus = certificateService.sendCertificate(
        request.getPatientId(),
        request.getCertificateId(),
        request.getRecipientId()
    );

    if (sendStatus != CertificateService.SendStatus.ALREADY_SENT) {
      statisticsService.sent(
          certificate.getId(),
          certificate.getType(),
          certificate.getCareUnitId(),
          request.getRecipientId()
      );

      internalNotificationService.notifyCareIfSentByCitizen(
          certificate,
          request.getPatientId().getOriginalPnr(),
          request.getHsaId()
      );
    }
    return sendStatus;
  }
}
