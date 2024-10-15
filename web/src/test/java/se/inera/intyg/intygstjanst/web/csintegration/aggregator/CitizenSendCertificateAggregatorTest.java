package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.csintegration.util.CertificateServiceProfile;
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
  @Mock
  CertificateServiceProfile certificateServiceProfile;
  CitizenSendCertificateAggregator aggregator;

  private static final SendCertificateRequestDTO REQUEST = SendCertificateRequestDTO.builder().build();

  @BeforeEach
  void setup() {
    certificateServiceProfile = mock(CertificateServiceProfile.class);

    aggregator = new CitizenSendCertificateAggregator(
        certificateServiceProfile,
        sendCertificateFromIT,
        sendCertificateFromCS
    );
  }

  @Test
  void shallReturnResponseFromIT()
      throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
    doReturn(false).when(certificateServiceProfile).active();
    aggregator.send(REQUEST);

    verify(sendCertificateFromIT).send(REQUEST);
    verifyNoInteractions(sendCertificateFromCS);
  }

  @Test
  void shallReturnResponseFromCS()
      throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
    doReturn(true).when(certificateServiceProfile).active();
    doReturn(SendStatus.OK).when(sendCertificateFromCS).send(REQUEST);

    aggregator.send(REQUEST);
    verify(sendCertificateFromCS).send(REQUEST);
    verifyNoInteractions(sendCertificateFromIT);
  }

  @Test
  void shallReturnResponseFromWCIfResponeFromCSIsNull()
      throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
    doReturn(true).when(certificateServiceProfile).active();
    doReturn(null).when(sendCertificateFromCS).send(REQUEST);
    doReturn(SendStatus.OK).when(sendCertificateFromIT).send(REQUEST);

    final var result = aggregator.send(REQUEST);
    assertEquals(SendStatus.OK, result);
  }
}