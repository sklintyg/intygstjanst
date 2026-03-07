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
package se.inera.intyg.intygstjanst.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.common.ts_parent.integration.RegisterCertificateV1Client;
import se.inera.intyg.common.ts_parent.integration.RegisterCertificateV3Client;
import se.inera.intyg.common.ts_parent.integration.SendTSClient;
import se.inera.intyg.common.ts_parent.integration.SendTSClientFactory;

@Configuration
public class TsBasItCxfConfig {

    /**
     * Exposes the configured register-certificate version string as a named bean.
     *
     * <p>Equivalent XML:
     * <pre>{@code
     * <bean id="tsBasRegisterCertificateVersion" class="java.lang.String">
     *   <constructor-arg value="${tsbas.send.certificate.to.recipient.registercertificate.version}"/>
     * </bean>
     * }</pre>
     */
    @Bean("tsBasRegisterCertificateVersion")
    public String tsBasRegisterCertificateVersion(
        @Value("${tsbas.send.certificate.to.recipient.registercertificate.version}") String version) {
        return version;
    }

    /**
     * Creates the {@link RegisterCertificateV1Client} for the ts-bas v1 endpoint.
     *
     * <p>Equivalent XML:
     * <pre>{@code
     * <bean id="tsBasRegisterCertificateV1Client"
     *       class="se.inera.intyg.common.ts_parent.integration.RegisterCertificateV1Client">
     *   <constructor-arg value="${registercertificatev1.endpoint.url}"/>
     * </bean>
     * }</pre>
     */
    @Bean("tsBasRegisterCertificateV1Client")
    public RegisterCertificateV1Client tsBasRegisterCertificateV1Client(
        @Value("${registercertificatev1.endpoint.url}") String url) {
        return new RegisterCertificateV1Client(url);
    }

    /**
     * Creates the {@link RegisterCertificateV3Client} for the ts-bas v3 endpoint.
     *
     * <p>Equivalent XML:
     * <pre>{@code
     * <bean id="tsBasRegisterCertificateV3Client"
     *       class="se.inera.intyg.common.ts_parent.integration.RegisterCertificateV3Client">
     *   <constructor-arg value="${registercertificatev3.endpoint.url}"/>
     * </bean>
     * }</pre>
     */
    @Bean("tsBasRegisterCertificateV3Client")
    public RegisterCertificateV3Client tsBasRegisterCertificateV3Client(
        @Value("${registercertificatev3.endpoint.url}") String url) {
        return new RegisterCertificateV3Client(url);
    }

    /**
     * Creates the {@link SendTSClientFactory} populated with both version clients.
     *
     * <p>Equivalent XML:
     * <pre>{@code
     * <util:map id="registerCertificateClientMap">
     *   <entry key="v1" value-ref="tsBasRegisterCertificateV1Client"/>
     *   <entry key="v3" value-ref="tsBasRegisterCertificateV3Client"/>
     * </util:map>
     * <bean id="sendTSClientFactory"
     *       class="se.inera.intyg.common.ts_parent.integration.SendTSClientFactory">
     *   <constructor-arg ref="registerCertificateClientMap"/>
     * </bean>
     * }</pre>
     */
    @Bean
    public SendTSClientFactory sendTSClientFactory(
        @Qualifier("tsBasRegisterCertificateV1Client") RegisterCertificateV1Client v1Client,
        @Qualifier("tsBasRegisterCertificateV3Client") RegisterCertificateV3Client v3Client) {
        final Map<String, SendTSClient> clientMap = new HashMap<>();
        clientMap.put("v1", v1Client);
        clientMap.put("v3", v3Client);
        return new SendTSClientFactory(clientMap);
    }
}