package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;

@ExtendWith(MockitoExtension.class)
class CitizenSendCertificateAggregatorTest {
  @Mock
  SendCertificateService sendCertificateFromIT;
  @Mock
  SendCertificateService sendCertificateFromCS;
  CitizenSendCertificateAggregator aggregator;

  private static final SendCertificateRequestDTO REQUEST = SendCertificateRequestDTO.builder().build();

  @BeforeEach
  void setup() {
    aggregator = new CitizenSendCertificateAggregator(
        sendCertificateFromIT,
        sendCertificateFromCS
    );
  }

  @Test
  void shallReturnResponseFromCS()
      throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
    doReturn(SendStatus.OK).when(sendCertificateFromCS).send(REQUEST);

    aggregator.send(REQUEST);
    verify(sendCertificateFromCS).send(REQUEST);
    verifyNoInteractions(sendCertificateFromIT);
  }

  @Test
  void shallReturnResponseFromITIfResponseFromCSIsNull()
      throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
    doReturn(null).when(sendCertificateFromCS).send(REQUEST);
    doReturn(SendStatus.OK).when(sendCertificateFromIT).send(REQUEST);

    final var result = aggregator.send(REQUEST);
    assertEquals(SendStatus.OK, result);
  }
}