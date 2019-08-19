/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.support.xml;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import org.springframework.core.io.ClassPathResource;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;

public final class XmlUnmarshallerUtil {

    private XmlUnmarshallerUtil() {
    }

    public static RevokeMedicalCertificateRequestType getRevokeMedicalCertificateRequestTypeFromFile(final String filePath)
        throws IOException {
        JAXBElement<RevokeMedicalCertificateRequestType> jaxbElement = XmlMarshallerHelper.unmarshal(getInputStream(filePath));
        return jaxbElement.getValue();
    }

    public static SendMessageToCareType getSendMessageToCareTypeFromFile(final String filePath) throws IOException {
        JAXBElement<SendMessageToCareType> jaxbElement = XmlMarshallerHelper.unmarshal(getInputStream(filePath));
        return jaxbElement.getValue();
    }

    private static InputStream getInputStream(String filePath) throws IOException {
        return new ClassPathResource(filePath).getInputStream();
    }
}
