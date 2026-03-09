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
import lombok.RequiredArgsConstructor;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificate.rivtabp20.v1.GetCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getmedicalcertificate.v1.GetMedicalCertificateResponderInterface;
import se.inera.intyg.common.fk7263.integration.GetCertificateResponderImpl;
import se.inera.intyg.common.fk7263.integration.GetMedicalCertificateResponderImpl;
import se.inera.intyg.common.fk7263.integration.RegisterMedicalCertificateResponderImpl;
import se.inera.intyg.common.support.modules.support.api.ModuleContainerApi;
import se.inera.intyg.common.util.integration.interceptor.SoapFaultToSoapResponseTransformerInterceptor;
import se.inera.intyg.intygstjanst.infrastructure.config.properties.AppProperties;

@Configuration
@RequiredArgsConstructor
public class Fk7263ItCxfConfig {

  private final Bus bus;
  private final AppProperties appProperties;

  @Bean
  public GetCertificateResponderInterface getCertificateResponder(
      ModuleContainerApi moduleContainer) {
    return new GetCertificateResponderImpl(moduleContainer);
  }

  @Bean
  public GetMedicalCertificateResponderInterface getMedicalCertificateResponder(
      ModuleContainerApi moduleContainer) {
    return new GetMedicalCertificateResponderImpl(moduleContainer);
  }

  @Bean
  public RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder(
      ModuleContainerApi moduleContainer) {
    return new RegisterMedicalCertificateResponderImpl(moduleContainer);
  }

  /**
   * Publishes the {@code GetCertificate v1} endpoint at {@code /get-certificate/v1.0}.
   *
   * <p>Equivalent XML:
   *
   * <pre>{@code
   * <jaxws:endpoint address="/get-certificate/v1.0"
   *                 implementor="se.inera.intyg.common.fk7263.integration.GetCertificateResponderImpl">
   *   <jaxws:outFaultInterceptors>
   *     <bean class="...SoapFaultToSoapResponseTransformerInterceptor">
   *       <constructor-arg value="transform/get-certificate-transform.xslt"/>
   *     </bean>
   *   </jaxws:outFaultInterceptors>
   * </jaxws:endpoint>
   * }</pre>
   */
  @Bean
  public Endpoint getCertificateEndpoint(GetCertificateResponderInterface implementor) {
    final EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/get-certificate-transform.xslt"));
    endpoint.publish("/get-certificate/v1.0");
    return endpoint;
  }

  /**
   * Publishes the {@code GetMedicalCertificate v1} endpoint at {@code
   * /get-medical-certificate/v1.0}.
   *
   * <p>Equivalent XML:
   *
   * <pre>{@code
   * <jaxws:endpoint address="/get-medical-certificate/v1.0"
   *                 implementor="se.inera.intyg.common.fk7263.integration.GetMedicalCertificateResponderImpl">
   *   <jaxws:outFaultInterceptors>
   *     <bean class="...SoapFaultToSoapResponseTransformerInterceptor">
   *       <constructor-arg value="transform/clinicalprocess-healthcond-certificate/get-medical-certificate-transform.xslt"/>
   *     </bean>
   *   </jaxws:outFaultInterceptors>
   * </jaxws:endpoint>
   * }</pre>
   */
  @Bean
  public Endpoint getMedicalCertificateEndpoint(
      GetMedicalCertificateResponderInterface implementor) {
    final EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-certificate/get-medical-certificate-transform.xslt"));
    endpoint.publish("/get-medical-certificate/v1.0");
    return endpoint;
  }

  /**
   * Publishes the {@code RegisterMedicalCertificate v3} endpoint at {@code
   * /register-certificate/v3.0}.
   *
   * <p>Equivalent XML:
   *
   * <pre>{@code
   * <jaxws:endpoint address="/register-certificate/v3.0"
   *                 implementor="se.inera.intyg.common.fk7263.integration.RegisterMedicalCertificateResponderImpl">
   *   <jaxws:outFaultInterceptors>
   *     <bean class="...SoapFaultToSoapResponseTransformerInterceptor">
   *       <constructor-arg value="transform/register-medical-certificate-transform.xslt"/>
   *     </bean>
   *   </jaxws:outFaultInterceptors>
   * </jaxws:endpoint>
   * }</pre>
   */
  @Bean
  public Endpoint registerMedicalCertificateEndpoint(
      @Qualifier("registerMedicalCertificateResponder") RegisterMedicalCertificateResponderInterface implementor) {
    final EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/register-medical-certificate-transform.xslt"));
    endpoint.publish("/register-certificate/v3.0");
    return endpoint;
  }

  /**
   * Creates the {@code registerMedicalCertificateClient} JAX-WS proxy client.
   *
   * <p>Equivalent XML:
   *
   * <pre>{@code
   * <jaxws:client id="registerMedicalCertificateClient"
   *               serviceClass="...RegisterMedicalCertificateResponderInterface"
   *               address="${registermedicalcertificatev3.endpoint.url}"/>
   * }</pre>
   */
  @Bean("registerMedicalCertificateClient")
  public RegisterMedicalCertificateResponderInterface registerMedicalCertificateClient() {
    final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(RegisterMedicalCertificateResponderInterface.class);
    factory.setAddress(appProperties.ntjp().endpoints().registerMedicalCertificateV3());
    return (RegisterMedicalCertificateResponderInterface) factory.create();
  }
}
