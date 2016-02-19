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
package se.inera.intyg.intygstjanst.web.integration.stub;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.inera.intyg.intygstjanst.web.integration.util.SendMessageToCareUtil;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareResponderStubTest {
    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    private String intygsIdNo1 = "intygsIdNo1";
    private String intygsIdNo2 = "intygsIdNo2";
    private String meddelandeIdNo1 = "meddelandeIdNo1";
    private String meddelandeIdNo2 = "meddelandeIdNo2";
    private String meddelandeIdNo3 = "meddelandeIdNo3";

    @InjectMocks
    private SendMessageToCareResponderStub stub = new SendMessageToCareResponderStub();

    @Spy
    private SendMessageToCareConverter converter = spy(new SendMessageToCareConverter());

    @Spy
    private SendMessageToCareStorage storage = spy(new SendMessageToCareStorage());

    @Test
    public void testSendMessageToCareResponderStub() throws Exception {
        String logicalAddress = "";
        SendMessageToCareType sendMessageToCareType1 = buildSendMessageToCare(intygsIdNo1, meddelandeIdNo1);
        stub.sendMessageToCare(logicalAddress, sendMessageToCareType1);
        verify(converter, times(1)).convertToXmlString(any(SendMessageToCareType.class));
        assertEquals(1, stub.getMessagesForCertificateId(sendMessageToCareType1.getIntygsId().getExtension()).size());
    }

    @Test
    public void testSendMultipleMessagesToCareResponderStubAndThenClear() throws Exception {
        String logicalAddress = "FK";
        SendMessageToCareType sendMessageToCareType1 = buildSendMessageToCare(intygsIdNo1, meddelandeIdNo1);
        SendMessageToCareType sendMessageToCareType2 = buildSendMessageToCare(intygsIdNo1, meddelandeIdNo2);
        SendMessageToCareType sendMessageToCareType3 = buildSendMessageToCare(intygsIdNo2, meddelandeIdNo3);
        stub.sendMessageToCare(logicalAddress, sendMessageToCareType1);
        stub.sendMessageToCare(logicalAddress, sendMessageToCareType2);
        stub.sendMessageToCare(logicalAddress, sendMessageToCareType3);
        verify(converter, times(3)).convertToXmlString(any(SendMessageToCareType.class));
        assertEquals(2, stub.getMessagesForCertificateId(intygsIdNo1).size());
        assertEquals(1, stub.getMessagesForCertificateId(intygsIdNo2).size());
        assertEquals(3, stub.getAllMessages().size());
        assertEquals(stub.getCount(), 3);
        stub.clear();
        assertEquals(0, stub.getCount());
    }

    private SendMessageToCareType buildSendMessageToCare(String intygsId, String meddelandeId) throws Exception {
        SendMessageToCareType sendMessageToCareType = SendMessageToCareUtil
                .getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        sendMessageToCareType.getIntygsId().setExtension(intygsId);
        sendMessageToCareType.setMeddelandeId(meddelandeId);
        return sendMessageToCareType;
    }

}
