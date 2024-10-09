/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

@ExtendWith(MockitoExtension.class)
class SoapIntegrationServiceImplTest {

  private static final String LOGICAL_ADDRESS = "123";
  private static final String RECIPIENT_ID = "id";
  @Mock
  private IntygModuleRegistry moduleRegistry;

  @Mock
  private RevokeCertificateResponderInterface revokeCertificateResponderInterface;

  @Mock
  private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

  @Mock
  private RevokeMedicalCertificateResponderInterface revokeMedicalCertificateResponderInterface;


  @InjectMocks
  SoapIntegrationServiceImpl soapIntegrationServiceImpl;

  @Test
  void shouldSendCertificateToRecipient() throws ModuleNotFoundException, ModuleException {
    final var certificate = new Certificate();
    final var originalCertificate = new OriginalCertificate();
    originalCertificate.setDocument("document");
    certificate.setOriginalCertificate(originalCertificate);
    certificate.setType("type");
    certificate.setTypeVersion("version");

    final var moduleApi = mock(ModuleApi.class);

    when(moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion()))
        .thenReturn(moduleApi);

    soapIntegrationServiceImpl.sendCertificateToRecipient(certificate, LOGICAL_ADDRESS, RECIPIENT_ID);

    verify(moduleApi, times(1)).sendCertificateToRecipient
        (certificate.getOriginalCertificate().getDocument(), LOGICAL_ADDRESS, RECIPIENT_ID);
  }

  @Test
  void shouldRevokeCertificate() {
    final var expected = new RevokeCertificateResponseType();
    final var request = new RevokeCertificateType();

    when(revokeCertificateResponderInterface.revokeCertificate(LOGICAL_ADDRESS, request))
        .thenReturn(expected);

    assertEquals(expected, soapIntegrationServiceImpl.revokeCertificate(LOGICAL_ADDRESS, request));
  }

  @Test
  void shouldRevokeMedicalCertificate() {
    final var expected = new RevokeMedicalCertificateResponseType();
    final var request = new RevokeMedicalCertificateRequestType();

    when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(new AttributedURIType(), request))
        .thenReturn(expected);

    assertEquals(expected, soapIntegrationServiceImpl.revokeMedicalCertificate(new AttributedURIType(), request));
  }

  @Test
  void shouldSendMessageToRecipient() {
    final var expected = new SendMessageToRecipientResponseType();
    final var request = new SendMessageToRecipientType();

    when(sendMessageToRecipientResponder.sendMessageToRecipient(LOGICAL_ADDRESS, request))
        .thenReturn(expected);

    assertEquals(expected, soapIntegrationServiceImpl.sendMessageToRecipient(LOGICAL_ADDRESS, request));
  }
}
