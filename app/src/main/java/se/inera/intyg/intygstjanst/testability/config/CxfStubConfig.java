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

package se.inera.intyg.intygstjanst.testability.config;

import jakarta.xml.ws.Endpoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.intygstjanst.testability.stub.RevokeCertificateResponderStub;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

@Configuration
@Profile("it-fk-stub")
@RequiredArgsConstructor
public class CxfStubConfig {

  private static final String SCHEMA_CP_3_3 =
      "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd";
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
  public RevokeCertificateResponderStub revokeCertificateClientStub() {
    return new RevokeCertificateResponderStub();
  }

  @Bean
  public Endpoint sendMessageToCareStubEndpoint(
      @Qualifier("sendMessageToCareResponderStub") SendMessageToCareResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/stubs/clinicalprocess/healthcond/certificate/SendMessageToCare/2/rivtabp21");
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
  public Endpoint sendMessageToRecipientStubEndpoint(
      @Qualifier("sendMessageToRecipientResponderStub") SendMessageToRecipientResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish(
        "/stubs/clinicalprocess/healthcond/certificate/SendMessageToRecipient/2/rivtabp21");
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
  public Endpoint revokeCertificateStubEndpoint(
      @Qualifier("revokeCertificateClientStub") RevokeCertificateResponderInterface implementor) {
    final var endpoint = new EndpointImpl(bus, implementor);
    endpoint.publish("/stubs/clinicalprocess/healthcond/certificate/RevokeCertificate/2/rivtabp21");
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
}
