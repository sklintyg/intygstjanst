package se.inera.intyg.intygstjanst.web.service.impl;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.integration.SendCertificateToRecipientResponderImpl;
import se.inera.intyg.intygstjanst.web.service.GetCitizenCertificateRecipientService;
import se.inera.intyg.intygstjanst.web.service.SendCitizenCertificateService;
import se.inera.intyg.intygstjanst.web.service.converter.SendCertificateToRecipientTypeConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.GetCitizenCertificateRecipientRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.SendCitizenCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.SendCitizenCertificateResponseDTO;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@Service
public class SendCitizenCertificateServiceImpl implements SendCitizenCertificateService {

  @Value("${intygstjanst.logicaladdress}")
  private String logicalAddress;

  private final SendCertificateToRecipientResponderInterface sendCertificateToRecipientResponder;
  private final GetCitizenCertificateRecipientService getCitizenCertificateRecipientService;

  public SendCitizenCertificateServiceImpl(
      SendCertificateToRecipientResponderImpl sendCertificateToRecipientResponder,
      GetCitizenCertificateRecipientService getCitizenCertificateRecipientService) {
    this.sendCertificateToRecipientResponder = sendCertificateToRecipientResponder;
    this.getCitizenCertificateRecipientService = getCitizenCertificateRecipientService;
  }


  @Override
  public SendCitizenCertificateResponseDTO send(SendCitizenCertificateRequestDTO request) {
    final var recipientResponse = getCitizenCertificateRecipientService.get(
        GetCitizenCertificateRecipientRequestDTO
            .builder()
            .certificateId(request.getCertificateId())
            .build()
    );

    final var formattedPatientId = Personnummer.createPersonnummer(request.getPatientId()).orElseThrow();
    final var sendRequest = SendCertificateToRecipientTypeConverter.convert(
        request.getCertificateId(),
        formattedPatientId,
        formattedPatientId, // Should patientId be same for both arguments?
        recipientResponse.getRecipient().getId() // Can either be sent through request or by getting recipient
    );


    final var response = sendCertificateToRecipientResponder.sendCertificateToRecipient(logicalAddress, sendRequest);

    if (response.getResult().getResultCode() == ResultCodeType.ERROR) {
      throw new RuntimeException(); // Which error do we want to throw?
    }

    return SendCitizenCertificateResponseDTO
        .builder()
        .sent(LocalDateTime.now()) // TODO: Change response, can't get exact time of sent since async action
        .build();
  }
}
