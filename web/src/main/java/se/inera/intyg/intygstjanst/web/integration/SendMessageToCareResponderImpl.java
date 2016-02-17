/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration;

import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();

        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        response.setResult(resultType);
        return response;
    }

}
