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

import jakarta.xml.ws.Endpoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.ifv.insuranceprocess.healthreporting.listcertificates.rivtabp20.v1.ListCertificatesResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.rivtabp20.v1.SendMedicalCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponderInterface;
import se.inera.intyg.common.util.integration.interceptor.SoapFaultToSoapResponseTransformerInterceptor;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

@Configuration
@RequiredArgsConstructor
public class CxfEndpointConfig {

  private static final String SCHEMA_CP_3_3 =
      "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd";
  private static final String SCHEMA_INTYG_TYPES_3_2 =
      "classpath:/core_components/intyg_clinicalprocess_healthcond_certificate_types_3.2.xsd";
  private static final String SCHEMA_CP_TYPES_3_2 =
      "classpath:/core_components/clinicalprocess_healthcond_certificate_types_3.2.xsd";
  private static final String SCHEMA_CP_3_2_EXT =
      "classpath:/core_components/clinicalprocess_healthcond_certificate_3.2_ext.xsd";
  private static final String SCHEMA_CP_3_4_EXT =
      "classpath:/core_components/clinicalprocess_healthcond_certificate_3.4_ext.xsd";
  private static final String SCHEMA_XMLDSIG =
      "classpath:/core_components/xmldsig-core-schema_0.1.xsd";
  private static final String SCHEMA_XMLDSIG_FILTER =
      "classpath:/core_components/xmldsig-filter2.xsd";

  private final Bus bus;

  @Bean
  public Endpoint getRecipientsForCertificateEndpoint(
      GetRecipientsForCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/get-recipients-for-certificate/v1.1");
    return endpoint;
  }

  @Bean
  public Endpoint listKnownRecipientsEndpoint(ListKnownRecipientsResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-known-recipients/v1.0");
    return endpoint;
  }

  @Bean
  public Endpoint listRelationsForCertificateEndpoint(
      ListRelationsForCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-relations-for-certificate/v1.0");
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_INTYG_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/ListRelationsForCertificateInteraction/ListRelationsForCertificateResponder_1.0.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint getCertificateTypeInfoEndpoint(
      GetCertificateTypeInfoResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/get-certificate-type-info/v1.0");
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_INTYG_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/GetCertificateTypeInfoInteraction/GetCertificateTypeInfoResponder_1.0.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint listCertificatesEndpoint(ListCertificatesResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-certificates/v1.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/list-certificates-transform.xslt"));
    return endpoint;
  }

  @Bean
  public Endpoint revokeMedicalCertificateEndpoint(
      RevokeMedicalCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/revoke-certificate/v1.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/revoke-medical-certificate-transform.xslt"));
    return endpoint;
  }

  @Bean
  public Endpoint revokeCertificateEndpoint(
      @Qualifier("revokeCertificateResponderImpl") RevokeCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/revoke-certificate-rivta/v2.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/revoke-certificate-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/RevokeCertificateInteraction/RevokeCertificateResponder_2.1.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint sendMedicalCertificateEndpoint(
      SendMedicalCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/send-certificate/v1.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/send-medical-certificate-transform.xslt"));
    return endpoint;
  }

  @Bean
  public Endpoint sendCertificateToRecipientEndpoint(
      SendCertificateToRecipientResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/send-certificate-to-recipient/v2.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/send-certificate-to-recipient-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/SendCertificateToRecipientInteraction/SendCertificateToRecipientResponder_2.1.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint sendMessageToCareEndpoint(
      @Qualifier("sendMessageToCareResponderImpl") SendMessageToCareResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/send-message-to-care/v2.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/send-message-to-care-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/SendMessageToCareInteraction/SendMessageToCareResponder_2.0.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint sendMessageToRecipientEndpoint(
      @Qualifier("sendMessageToRecipientResponderImpl") SendMessageToRecipientResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/send-message-to-recipient/v2.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/send-message-to-recipient-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/SendMessageToRecipientInteraction/SendMessageToRecipientResponder_2.1.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint setCertificateStatusEndpoint(
      @Qualifier("setCertificateStatusResponderV1") se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1
                  .SetCertificateStatusResponderInterface
              implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/set-certificate-status/v1.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/set-certificate-status-transform.xslt"));
    return endpoint;
  }

  @Bean
  public Endpoint registerCertificateEndpoint(
      @Qualifier("registerCertificateResponderImpl") RegisterCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/register-certificate-se/v3.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/register-certificate-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_INTYG_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/RegisterCertificateInteraction/RegisterCertificateResponder_3.1.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint getCertificateV2Endpoint(GetCertificateResponderInterface implementor) {
    // No outFaultInterceptors — we want a SOAPFault when the certificate does not exist. See
    // INTYG-2126.
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/get-certificate-se/v2.0");
    return endpoint;
  }

  @Bean
  public Endpoint listCertificatesForCitizenV3Endpoint(
      @Qualifier("ListCertificatesForCitizenResponderImplV3") se.riv
                  .clinicalprocess
                  .healthcond
                  .certificate
                  .listCertificatesForCitizen
                  .v3
                  .ListCertificatesForCitizenResponderInterface
              implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-certificates-for-citizen/v3.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/list-certificates-for-citizen-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/ListCertificatesForCitizenInteraction/ListCertificatesForCitizenResponder_3.0.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint listCertificatesForCitizenV4Endpoint(
      @Qualifier("listCertificatesForCitizenResponderImpl") se.riv
                  .clinicalprocess
                  .healthcond
                  .certificate
                  .listCertificatesForCitizen
                  .v4
                  .ListCertificatesForCitizenResponderInterface
              implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-certificates-for-citizen/v4.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-4/list-certificates-for-citizen-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/ListCertificatesForCitizenInteraction/ListCertificatesForCitizenResponder_4.0.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint listCertificatesForCareV3Endpoint(
      ListCertificatesForCareResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-certificates-for-care/v3.0");
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/ListCertificatesForCareInteraction/ListCertificatesForCareResponder_3.1.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint setCertificateStatusV2Endpoint(
      @Qualifier("setCertificateStatusResponderImplV2") se.riv
                  .clinicalprocess
                  .healthcond
                  .certificate
                  .setCertificateStatus
                  .v2
                  .SetCertificateStatusResponderInterface
              implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/set-certificate-status-rivta/v2.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate-3/set-certificate-status-transform.xslt"));
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/SetCertificateStatusInteraction/SetCertificateStatusResponder_2.0.xsd"));
    return endpoint;
  }

  @Bean
  public Endpoint listApprovedReceiversEndpoint(
      ListApprovedReceiversResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-approved-receivers/v1.0");
    return endpoint;
  }

  @Bean
  public Endpoint listPossibleReceiversEndpoint(
      ListPossibleReceiversResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-possible-receivers/v1.0");
    return endpoint;
  }

  @Bean
  public Endpoint registerApprovedReceiversEndpoint(
      RegisterApprovedReceiversResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/register-approved-receivers/v1.0");
    return endpoint;
  }

  @Bean
  public Endpoint listActiveSickLeavesForCareUnitEndpoint(
      ListActiveSickLeavesForCareUnitResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-active-sick-leaves-for-care-unit/v1.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-rehabilitation/list-active-sick-leaves-for-care-unit-transform.xslt"));
    return endpoint;
  }

  @Bean
  public Endpoint listSickLeavesForPersonEndpoint(
      ListSickLeavesForPersonResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-sick-leaves-for-person/v1.0");
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-rehabilitation/list-sick-leaves-for-person-transform.xslt"));
    return endpoint;
  }

  @Bean
  public Endpoint listSickLeavesForCareEndpoint(
      ListSickLeavesForCareResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/list-sickleaves-for-care/v1.0");
    endpoint.setSchemaLocations(
        List.of(
            SCHEMA_CP_3_3,
            SCHEMA_CP_TYPES_3_2,
            SCHEMA_CP_3_2_EXT,
            SCHEMA_CP_3_4_EXT,
            SCHEMA_XMLDSIG,
            SCHEMA_XMLDSIG_FILTER,
            "classpath:/interactions/ListSickLeavesForCareInteraction/ListSickLeavesForCareResponder_1.0.xsd"));
    return endpoint;
  }
}
