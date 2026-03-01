/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.config;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

@Configuration
public class CxfClientConfig {

    @Bean("revokeMedicalCertificateClient")
    public RevokeMedicalCertificateResponderInterface revokeMedicalCertificateClient(
        @Value("${revokemedicalcertificatev1.endpoint.url}") String address) {
        final var factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(RevokeMedicalCertificateResponderInterface.class);
        factory.setAddress(address);
        return (RevokeMedicalCertificateResponderInterface) factory.create();
    }

    @Bean("sendMedicalCertificateQuestionClient")
    public SendMedicalCertificateQuestionResponderInterface sendMedicalCertificateQuestionClient(
        @Value("${sendmedicalcertificatequestionv1.endpoint.url}") String address) {
        final var factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SendMedicalCertificateQuestionResponderInterface.class);
        factory.setAddress(address);
        return (SendMedicalCertificateQuestionResponderInterface) factory.create();
    }

    @Bean("sendMessageToCareClient")
    public SendMessageToCareResponderInterface sendMessageToCareClient(
        @Value("${sendmessagetocarev2.endpoint.url}") String address) {
        final var factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SendMessageToCareResponderInterface.class);
        factory.setAddress(address);
        return (SendMessageToCareResponderInterface) factory.create();
    }

    @Bean("sendMessageToRecipientClient")
    public SendMessageToRecipientResponderInterface sendMessageToRecipientClient(
        @Value("${sendmessagetorecipientv2.endpoint.url}") String address) {
        final var factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SendMessageToRecipientResponderInterface.class);
        factory.setAddress(address);
        return (SendMessageToRecipientResponderInterface) factory.create();
    }

    @Bean("revokeCertificateClient")
    public RevokeCertificateResponderInterface revokeCertificateClient(
        @Value("${revokecertificatev2.endpoint.url}") String address) {
        final var factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(RevokeCertificateResponderInterface.class);
        factory.setAddress(address);
        return (RevokeCertificateResponderInterface) factory.create();
    }

    @Bean("registerCertificateClient")
    public RegisterCertificateResponderInterface registerCertificateClient(
        @Value("${registercertificatev3.endpoint.url}") String address) {
        final var factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(RegisterCertificateResponderInterface.class);
        factory.setAddress(address);
        return (RegisterCertificateResponderInterface) factory.create();
    }
}

