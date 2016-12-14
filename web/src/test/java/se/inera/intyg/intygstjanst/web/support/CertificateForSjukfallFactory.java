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
package se.inera.intyg.intygstjanst.web.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import se.inera.intyg.common.fk7263.model.internal.Utlatande;
import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;

/**
 * Created by eriklupander on 2016-02-15.
 */
public class CertificateForSjukfallFactory {

    private static final String CERT_ID = " cert-123 ";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String FNAME = "Tolvan";
    private static final String ENAME = "Tolvansson";
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOC_NAME = "Doc Name";
    private static final String CERT_TYPE = "fk7263";
    private static final String START_DATE_100 = "2016-02-01";
    private static final String END_DATE_100 = "2216-02-01";
    private static final String START_DATE_75 = "2016-03-01";
    private static final String END_DATE_75 = "2216-03-01";
    private static final String START_DATE_50 = "2016-04-01";
    private static final String END_DATE_50 = "2216-04-01";
    private static final String START_DATE_25 = "2016-05-01";
    private static final String END_DATE_25 = "2216-05-01";

    private static final String DOC_ID = " doc-1 ";
    private static final String CARE_UNIT_ID = " enhet-1 ";
    private static final String CARE_UNIT_NAME = "Enhet1";
    private static final String CARE_GIVER_ID = " vardgivare-1 ";

    private Personnummer pNr = new Personnummer(PERSONNUMMER);

    private static CertificateForSjukfallFactory factory;

    private CertificateForSjukfallFactory() {

    }

    public static CertificateForSjukfallFactory getFactoryInstance() {
        if (factory == null) {
            factory = new CertificateForSjukfallFactory();
        }
        return factory;
    }

    // TODOO Merge the code below with code from CertToSjukfall converter test. To utility
    public Utlatande buildUtlatande() {
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grundData = mock(GrundData.class);
        Vardenhet vardenhet = mock(Vardenhet.class);
        Vardgivare vardgivare = mock(Vardgivare.class);

        HoSPersonal hoSPersonal = mock(HoSPersonal.class);
        Patient patient = mock(Patient.class);

        when(utlatande.getGrundData()).thenReturn(grundData);
        when(grundData.getPatient()).thenReturn(patient);
        when(grundData.getSkapadAv()).thenReturn(hoSPersonal);
        when(hoSPersonal.getPersonId()).thenReturn(DOC_ID);
        when(hoSPersonal.getFullstandigtNamn()).thenReturn(DOC_NAME);
        when(hoSPersonal.getVardenhet()).thenReturn(vardenhet);
        when(vardenhet.getEnhetsid()).thenReturn(CARE_UNIT_ID);
        when(vardenhet.getVardgivare()).thenReturn(vardgivare);
        when(vardgivare.getVardgivarid()).thenReturn(CARE_GIVER_ID);
        when(patient.getFornamn()).thenReturn(FNAME);
        when(patient.getEfternamn()).thenReturn(ENAME);

        when(utlatande.getNedsattMed100()).thenReturn(new InternalLocalDateInterval(START_DATE_100, END_DATE_100));
        when(utlatande.getNedsattMed75()).thenReturn(new InternalLocalDateInterval(START_DATE_75, END_DATE_75));
        when(utlatande.getNedsattMed50()).thenReturn(new InternalLocalDateInterval(START_DATE_50, END_DATE_50));
        when(utlatande.getNedsattMed25()).thenReturn(new InternalLocalDateInterval(START_DATE_25, END_DATE_25));

        return utlatande;
    }

    public Certificate buildCert() {
        Certificate cert = new Certificate(CERT_ID);
        cert.setType(CERT_TYPE);
        cert.setSignedDate(CERT_SIGNING_DATETIME);
        cert.setSigningDoctorName(DOC_NAME);
        cert.setCivicRegistrationNumber(pNr);
        cert.setCareGiverId(CARE_GIVER_ID);
        cert.setCareUnitId(CARE_UNIT_ID);
        cert.setCareUnitName(CARE_UNIT_NAME);
        cert.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "XML", cert));
        return cert;
    }

}
