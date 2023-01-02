/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import se.inera.intyg.common.ag114.v1.model.internal.Ag114UtlatandeV1;
import se.inera.intyg.common.ag114.v1.model.internal.Sysselsattning;
import se.inera.intyg.common.ag114.v1.model.internal.Sysselsattning.SysselsattningsTyp;
import se.inera.intyg.common.ag7804.model.internal.Sjukskrivning;
import se.inera.intyg.common.ag7804.model.internal.Sjukskrivning.SjukskrivningsGrad;
import se.inera.intyg.common.ag7804.v1.model.internal.Ag7804UtlatandeV1;
import se.inera.intyg.common.agparent.model.internal.Diagnos;
import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

public class CertificateToSickLeaveCertificateConverterTest {

    private static final String CERT_ID = "cert-123";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String NAME = "Tolvan Tolvansson";
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOC_NAME = "Doc Name";
    private static final String CERT_TYPE_AG7804 = "ag7804";
    private static final String CERT_TYPE_AG114 = "ag114";

    private static final String START_DATE_100 = "2016-02-01";
    private static final String END_DATE_100 = "2216-02-01";
    private static final String START_DATE_75 = "2016-03-01";
    private static final String END_DATE_75 = "2216-03-01";
    private static final String START_DATE_50 = "2016-04-01";
    private static final String END_DATE_50 = "2216-04-01";
    private static final String START_DATE_25 = "2016-05-01";
    private static final String END_DATE_25 = "2216-05-01";

    private static final String DOC_ID = "doc-1";
    private static final String CARE_UNIT_ID = "enhet-1";
    private static final String CARE_UNIT_NAME = "Enhet1";
    private static final String CARE_GIVER_ID = "vardgivare-1";
    private static final String DIAGNOSE_CODE = "diag-1";
    private static final String DIAGNOSE_CODE_2 = "diag-2";
    private static final String OCCUPATION = "Nuvarande arbete";
    private static final String HUNDRED_PERCENT = "100";


    private final Personnummer pNr = Personnummer.createPersonnummer(PERSONNUMMER).get();


    @Test
    public void convertAg7804() {
        Certificate certificate = buildCertificate(CERT_TYPE_AG7804);
        Ag7804UtlatandeV1 statement = buildAg7804Statement();

        var sickLeaveCertificate = (new CertificateToSickLeaveCertificateConverter()).convertAg7804(certificate, statement);

        assertEquals(CERT_ID, sickLeaveCertificate.getCertificateId());
        assertEquals(CERT_TYPE_AG7804, sickLeaveCertificate.getCertificateType());

        assertEquals(CERT_SIGNING_DATETIME, sickLeaveCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, sickLeaveCertificate.getCareProviderId());
        assertEquals(CARE_UNIT_ID, sickLeaveCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sickLeaveCertificate.getCareUnitName());

        assertEquals(DOC_ID, sickLeaveCertificate.getPersonalHsaId());
        assertEquals(DOC_NAME, sickLeaveCertificate.getPersonalFullName());
        assertEquals(NAME, sickLeaveCertificate.getPatientFullName());
        assertEquals(PERSONNUMMER, sickLeaveCertificate.getPersonId());

        assertEquals(DIAGNOSE_CODE, sickLeaveCertificate.getDiagnoseCode());
        assertEquals(DIAGNOSE_CODE_2, sickLeaveCertificate.getSecondaryDiagnoseCodes().get(0));

        assertEquals(OCCUPATION, sickLeaveCertificate.getOccupation());

        var workCapacityList = sickLeaveCertificate.getWorkCapacityList();

        assertEquals(4, workCapacityList.size());

        assertEquals(100, workCapacityList.get(0).getReduction());
        assertEquals(LocalDate.parse(START_DATE_100), workCapacityList.get(0).getStartDate());
        assertEquals(LocalDate.parse(END_DATE_100), workCapacityList.get(0).getEndDate());

        assertEquals(75, workCapacityList.get(1).getReduction());
        assertEquals(LocalDate.parse(START_DATE_75), workCapacityList.get(1).getStartDate());
        assertEquals(LocalDate.parse(END_DATE_75), workCapacityList.get(1).getEndDate());

        assertEquals(50, workCapacityList.get(2).getReduction());
        assertEquals(LocalDate.parse(START_DATE_50), workCapacityList.get(2).getStartDate());
        assertEquals(LocalDate.parse(END_DATE_50), workCapacityList.get(2).getEndDate());

        assertEquals(25, workCapacityList.get(3).getReduction());
        assertEquals(LocalDate.parse(START_DATE_25), workCapacityList.get(3).getStartDate());
        assertEquals(LocalDate.parse(END_DATE_25), workCapacityList.get(3).getEndDate());

    }

    @Test
    public void convertAg114() {
        Certificate certificate = buildCertificate(CERT_TYPE_AG114);
        Ag114UtlatandeV1 statement = buildAg114Statement();

        var sickLeaveCertificate = (new CertificateToSickLeaveCertificateConverter()).convertAg114(certificate, statement);

        assertEquals(CERT_ID, sickLeaveCertificate.getCertificateId());
        assertEquals(CERT_TYPE_AG114, sickLeaveCertificate.getCertificateType());

        assertEquals(CERT_SIGNING_DATETIME, sickLeaveCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, sickLeaveCertificate.getCareProviderId());
        assertEquals(CARE_UNIT_ID, sickLeaveCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sickLeaveCertificate.getCareUnitName());

        assertEquals(DOC_ID, sickLeaveCertificate.getPersonalHsaId());
        assertEquals(DOC_NAME, sickLeaveCertificate.getPersonalFullName());
        assertEquals(NAME, sickLeaveCertificate.getPatientFullName());
        assertEquals(PERSONNUMMER, sickLeaveCertificate.getPersonId());

        assertEquals(DIAGNOSE_CODE, sickLeaveCertificate.getDiagnoseCode());
        assertEquals(DIAGNOSE_CODE_2, sickLeaveCertificate.getSecondaryDiagnoseCodes().get(0));

        assertEquals(OCCUPATION, sickLeaveCertificate.getOccupation());

        assertEquals(sickLeaveCertificate.getWorkCapacityList().size(), 1);
        var workCapacity = sickLeaveCertificate.getWorkCapacityList().get(0);
        assertEquals(Integer.parseInt(HUNDRED_PERCENT), workCapacity.getReduction());
        assertEquals(LocalDate.parse(START_DATE_100), workCapacity.getStartDate());
        assertEquals(LocalDate.parse(END_DATE_100), workCapacity.getEndDate());

    }

    private Ag114UtlatandeV1 buildAg114Statement() {
        var statement = mock(Ag114UtlatandeV1.class);
        when(statement.getDiagnoser()).thenReturn(getDiagnoses());
        when(statement.getGrundData()).thenReturn(getBasicData());
        when(statement.getSjukskrivningsgrad()).thenReturn(HUNDRED_PERCENT);
        when(statement.getSjukskrivningsperiod()).thenReturn(getSickLeavePeriod());
        when(statement.getSysselsattning()).thenReturn(getSysselsattningAg114());
        return statement;
    }

    private InternalLocalDateInterval getSickLeavePeriod() {
        return new InternalLocalDateInterval(START_DATE_100, END_DATE_100);
    }

    private ImmutableList<se.inera.intyg.common.ag114.v1.model.internal.Sysselsattning> getSysselsattningAg114() {
        var occupation = se.inera.intyg.common.ag114.v1.model.internal.Sysselsattning.create(SysselsattningsTyp.NUVARANDE_ARBETE);
        return ImmutableList.copyOf(new Sysselsattning[]{occupation});
    }

    private Ag7804UtlatandeV1 buildAg7804Statement() {
        var statement = mock(Ag7804UtlatandeV1.class);
        when(statement.getDiagnoser()).thenReturn(getDiagnoses());
        when(statement.getGrundData()).thenReturn(getBasicData());
        when(statement.getSjukskrivningar()).thenReturn(getSickLeaves());
        when(statement.getSysselsattning()).thenReturn(getOccupationAg7804());
        return statement;
    }

    private ImmutableList<Sjukskrivning> getSickLeaves() {
        return ImmutableList.copyOf(
            new Sjukskrivning[]{
                Sjukskrivning.create(SjukskrivningsGrad.HELT_NEDSATT, new InternalLocalDateInterval(START_DATE_100, END_DATE_100)),
                Sjukskrivning.create(SjukskrivningsGrad.NEDSATT_3_4, new InternalLocalDateInterval(START_DATE_75, END_DATE_75)),
                Sjukskrivning.create(SjukskrivningsGrad.NEDSATT_HALFTEN, new InternalLocalDateInterval(START_DATE_50, END_DATE_50)),
                Sjukskrivning.create(SjukskrivningsGrad.NEDSATT_1_4, new InternalLocalDateInterval(START_DATE_25, END_DATE_25))
            });
    }

    private ImmutableList<se.inera.intyg.common.ag7804.model.internal.Sysselsattning> getOccupationAg7804() {
        var occupation = se.inera.intyg.common.ag7804.model.internal.Sysselsattning.create(
            se.inera.intyg.common.ag7804.model.internal.Sysselsattning.SysselsattningsTyp.NUVARANDE_ARBETE);
        return ImmutableList.copyOf(new se.inera.intyg.common.ag7804.model.internal.Sysselsattning[]{occupation});
    }

    private ImmutableList<Diagnos> getDiagnoses() {
        return ImmutableList
            .copyOf(java.util.List.of(Diagnos.create(DIAGNOSE_CODE, null, null, null),
                Diagnos.create(DIAGNOSE_CODE_2, null, null, null)));
    }

    private GrundData getBasicData() {
        var basicData = new GrundData();

        var patient = new Patient();
        patient.setFullstandigtNamn(NAME);
        patient.setPersonId(pNr);

        basicData.setPatient(patient);
        basicData.setSigneringsdatum(CERT_SIGNING_DATETIME);

        var hoSPersonal = new HoSPersonal();
        hoSPersonal.setPersonId(DOC_ID);
        hoSPersonal.setFullstandigtNamn(DOC_NAME);
        basicData.setSkapadAv(hoSPersonal);

        return basicData;
    }

    private Certificate buildCertificate(String type) {
        var certificate = new Certificate(CERT_ID);
        certificate.setType(type);
        certificate.setTypeVersion("1.0");
        certificate.setSignedDate(CERT_SIGNING_DATETIME);
        certificate.setSigningDoctorName(DOC_NAME);
        certificate.setCivicRegistrationNumber(pNr);
        certificate.setCareGiverId(CARE_GIVER_ID);
        certificate.setCareUnitId(CARE_UNIT_ID);
        certificate.setCareUnitName(CARE_UNIT_NAME);
        certificate.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "XML", certificate));
        return certificate;
    }
}