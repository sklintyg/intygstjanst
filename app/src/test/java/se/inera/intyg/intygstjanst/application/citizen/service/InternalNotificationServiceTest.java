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
package se.inera.intyg.intygstjanst.application.citizen.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.intygstjanst.infrastructure.config.properties.AppProperties;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Certificate;

@ExtendWith(MockitoExtension.class)
class InternalNotificationServiceTest {

  private static final String INTYG_ID = "intyg-1";
  private static final String ENHET_ID = "enhet-1";
  private static final String INTYG_TYP = "lijsp";
  private static final String PERSON_ID = "personId";
  private static final String HSA_ID = "hsaId";

  @Mock private JmsTemplate jmsTemplate;

  @Mock private AppProperties appProperties;

  @InjectMocks private InternalNotificationService testee;

  @BeforeEach
  void setUp() {
    Mockito.lenient()
        .when(appProperties.jms())
        .thenReturn(
            new AppProperties.Jms(
                "certificate.queue", "internal.notification.queue", "event.queue", false));
  }

  @Test
  void testSendsNotificationIfSentByCitizen() {
    testee.notifyCareIfSentByCitizen(buildCert(), PERSON_ID, null);
    verify(jmsTemplate, times(1)).send(anyString(), any(MessageCreator.class));
  }

  @Test
  void testDoesNotNotifiyWhenHsaIdIsDefined() {
    testee.notifyCareIfSentByCitizen(buildCert(), PERSON_ID, HSA_ID);
    verifyNoInteractions(jmsTemplate);
  }

  @Test
  void facadeCertificateSendsNotificationIfSentByCitizen() {
    final var certificate = new se.inera.intyg.common.support.facade.model.Certificate();
    certificate.setMetadata(
        CertificateMetadata.builder()
            .id("id")
            .type("type")
            .typeVersion("typeVersion")
            .unit(Unit.builder().unitId("unitId").build())
            .build());
    testee.notifyCareIfSentByCitizen(certificate, PERSON_ID, null);
    verify(jmsTemplate, times(1)).send(anyString(), any(MessageCreator.class));
  }

  @Test
  void facadeCertificateDoesNotNotifiyWhenHsaIdIsDefined() {
    testee.notifyCareIfSentByCitizen(
        new se.inera.intyg.common.support.facade.model.Certificate(), PERSON_ID, HSA_ID);
    verifyNoInteractions(jmsTemplate);
  }

  private Certificate buildCert() {
    Certificate c = new Certificate(INTYG_ID);
    c.setCareUnitId(ENHET_ID);
    c.setType(INTYG_TYP);
    return c;
  }
}
