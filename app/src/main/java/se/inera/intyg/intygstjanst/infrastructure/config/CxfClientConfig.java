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

package se.inera.intyg.intygstjanst.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.intyg.intygstjanst.infrastructure.config.properties.AppProperties;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

@Configuration
@RequiredArgsConstructor
public class CxfClientConfig {

  private final AppProperties appProperties;

  @Bean("revokeMedicalCertificateClient")
  public RevokeMedicalCertificateResponderInterface revokeMedicalCertificateClient() {
    final var factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(RevokeMedicalCertificateResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().revokeMedicalCertificateV1());
    return (RevokeMedicalCertificateResponderInterface) factory.create();
  }

  @Bean("sendMedicalCertificateQuestionClient")
  public SendMedicalCertificateQuestionResponderInterface sendMedicalCertificateQuestionClient() {
    final var factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(SendMedicalCertificateQuestionResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().sendMedicalCertificateQuestionV1());
    return (SendMedicalCertificateQuestionResponderInterface) factory.create();
  }

  @Bean("sendMessageToCareClient")
  public SendMessageToCareResponderInterface sendMessageToCareClient() {
    final var factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(SendMessageToCareResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().sendMessageToCareV2());
    return (SendMessageToCareResponderInterface) factory.create();
  }

  @Bean("sendMessageToRecipientClient")
  public SendMessageToRecipientResponderInterface sendMessageToRecipientClient() {
    final var factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(SendMessageToRecipientResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().sendMessageToRecipientV2());
    return (SendMessageToRecipientResponderInterface) factory.create();
  }

  @Bean("revokeCertificateClient")
  public RevokeCertificateResponderInterface revokeCertificateClient() {
    final var factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(RevokeCertificateResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().revokeCertificateV2());
    return (RevokeCertificateResponderInterface) factory.create();
  }

  @Bean("registerCertificateClient")
  public RegisterCertificateResponderInterface registerCertificateClient() {
    final var factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(RegisterCertificateResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().registerCertificateV3());
    return (RegisterCertificateResponderInterface) factory.create();
  }
}
