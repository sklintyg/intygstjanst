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
package se.inera.intyg.intygstjanst.web.integration.validator;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.intyg.common.fk7263.schemas.insuranceprocess.healthreporting.validator.VardAdresseringsTypeValidator;
import se.inera.intyg.common.support.validate.CertificateValidationException;

public class SendCertificateRequestValidator {

    private SendType sendRequest = null;
    private List<String> validationErrors = new ArrayList<>();

    public SendCertificateRequestValidator(SendType sendRequest) {
        this.sendRequest = sendRequest;
    }

    public void validateAndCorrect() throws CertificateValidationException {
        // First, validate properties at Revoke request level
        if (Strings.isNullOrEmpty(sendRequest.getVardReferensId())) {
            validationErrors.add("No vardReferens found!");
        }
        if (sendRequest.getAvsantTidpunkt() == null) {
            validationErrors.add("No avsantTidpunkt found!");
        }

        // use commmon validators for common elements
        new LakarutlatandeEnkelTypeValidator(sendRequest.getLakarutlatande(), validationErrors).validateAndCorrect();
        new VardAdresseringsTypeValidator(sendRequest.getAdressVard(), validationErrors).validateAndCorrect();

        if (!validationErrors.isEmpty()) {
            throw new CertificateValidationException(validationErrors);
        }
    }
}
