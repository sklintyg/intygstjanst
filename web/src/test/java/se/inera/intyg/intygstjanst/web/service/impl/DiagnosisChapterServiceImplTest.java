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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKategori;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterProvider;

@ExtendWith(MockitoExtension.class)
class DiagnosisChapterServiceImplTest {

    @InjectMocks
    private DiagnosisChapterServiceImpl diagnosisChapterService;
    @Mock
    private DiagnosisChapterProvider diagnosisChapterProvider;

    private static final String ID = "id";
    private static final String DIAGNOSIS_CODE = "A10";

    @BeforeEach
    void setUp() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        when(diagnosisChapterProvider.getDiagnosisChapters()).thenReturn(getChapters());

        Method postConstruct = DiagnosisChapterServiceImpl.class.getDeclaredMethod("init", null);
        postConstruct.setAccessible(true);
        postConstruct.invoke(diagnosisChapterService);

    }

    @Test
    void shouldReturnChapterA() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("A01");
        final var expectedResult = List.of(new DiagnosKapitel("A00-B99Vissa infektionssjukdomar och parasitsjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterB() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("B01");
        final var expectedResult = List.of(new DiagnosKapitel("A00-B99Vissa infektionssjukdomar och parasitsjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterC() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("C01");
        final var expectedResult = List.of(new DiagnosKapitel("C00-D48Tumörer"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterD() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("D48");
        final var expectedResult = List.of(new DiagnosKapitel("C00-D48Tumörer"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterE() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("E13");
        final var expectedResult = List.of(
            new DiagnosKapitel("E00-E90Endokrina sjukdomar, nutritionsrubbningar och ämnesomsättningssjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterF() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("F13");
        final var expectedResult = List.of(
            new DiagnosKapitel("F00-F99Psykiska sjukdomar och syndrom samt beteendestörningar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterG() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("G10");
        final var expectedResult = List.of(
            new DiagnosKapitel("G00-G99Sjukdomar i nervsystemet"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnFirstChapterH() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("H10");
        final var expectedResult = List.of(
            new DiagnosKapitel("H00-H59Sjukdomar i ögat och närliggande organ"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnSecondChapterH() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("H60");
        final var expectedResult = List.of(
            new DiagnosKapitel("H60-H95Sjukdomar i örat och mastoidutskottet"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterI() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("I50");
        final var expectedResult = List.of(
            new DiagnosKapitel("I00-I99Cirkulationsorganens sjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterJ() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("J13");
        final var expectedResult = List.of(
            new DiagnosKapitel("J00-J99Andningsorganens sjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterK() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("K10");
        final var expectedResult = List.of(
            new DiagnosKapitel("K00-K93Matsmältningsorganens sjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterL() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("L10");
        final var expectedResult = List.of(
            new DiagnosKapitel("L00-L99Hudens och underhudens sjukdomar"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterM() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("M10");
        final var expectedResult = List.of(
            new DiagnosKapitel("M00-M99Sjukdomar i muskuloskeletala systemet och bindväven"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterN() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("N10");
        final var expectedResult = List.of(
            new DiagnosKapitel("N00-N99Sjukdomar i urin- och könsorganen"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterO() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("O10");
        final var expectedResult = List.of(
            new DiagnosKapitel("O00-O99Graviditet, förlossning och barnsängstid"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterP() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("P10");
        final var expectedResult = List.of(
            new DiagnosKapitel("P00-P96Vissa perinatala tillstånd"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterQ() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("Q10");
        final var expectedResult = List.of(
            new DiagnosKapitel("Q00-Q99Medfödda missbildningar, deformiteter och kromosomavvikelser"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterR() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("R10");
        final var expectedResult = List.of(
            new DiagnosKapitel(
                "R00-R99Symtom, sjukdomstecken och onormala kliniska fynd och laboratoriefynd som ej klassificeras på annan plats"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterS() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("S10");
        final var expectedResult = List.of(
            new DiagnosKapitel(
                "S00-T98Skador, förgiftningar och vissa andra följder av yttre orsaker"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterU() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("U10");
        final var expectedResult = List.of(
            new DiagnosKapitel(
                "U00-U99Koder för särskilda ändamål"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterV() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("V10");
        final var expectedResult = List.of(
            new DiagnosKapitel(
                "V01-Y98Yttre orsaker till sjukdom och död"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterZ() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("Z10");
        final var expectedResult = List.of(
            new DiagnosKapitel(
                "Z00-Z99Faktorer av betydelse för hälsotillståndet och för kontakter med hälso- och sjukvården"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnChapterNotFound() {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode("K99");
        final var expectedResult = List.of(
            new DiagnosKapitel(new DiagnosKategori(' ', 0), new DiagnosKategori(' ', 0), "Utan giltig diagnoskod"));
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(List.of(sjukfallCertificate));
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldGetChapterForSickLeaveUnit() {
        final var sickLeaveUnit = new SjukfallEnhet();
        sickLeaveUnit.setDiagnosKod(DiagnosKod.create(DIAGNOSIS_CODE));
        final var expectedResult = new DiagnosKapitel("A00-B99Vissa infektionssjukdomar och parasitsjukdomar");
        final var result = diagnosisChapterService.getDiagnosisChaptersFromSickLeave(sickLeaveUnit);
        assertEquals(expectedResult, result);
    }

    private List<DiagnosKapitel> getChapters() {
        return List.of(new DiagnosKapitel("A00-B99Vissa infektionssjukdomar och parasitsjukdomar"),
            new DiagnosKapitel("C00-D48Tumörer"),
            new DiagnosKapitel("D50-D89Sjukdomar i blod och blodbildande organ samt vissa rubbningar i immunsystemet"),
            new DiagnosKapitel("E00-E90Endokrina sjukdomar, nutritionsrubbningar och ämnesomsättningssjukdomar"),
            new DiagnosKapitel("F00-F99Psykiska sjukdomar och syndrom samt beteendestörningar"),
            new DiagnosKapitel("G00-G99Sjukdomar i nervsystemet"),
            new DiagnosKapitel("H00-H59Sjukdomar i ögat och närliggande organ"),
            new DiagnosKapitel("H60-H95Sjukdomar i örat och mastoidutskottet"),
            new DiagnosKapitel("I00-I99Cirkulationsorganens sjukdomar"),
            new DiagnosKapitel("J00-J99Andningsorganens sjukdomar"),
            new DiagnosKapitel("K00-K93Matsmältningsorganens sjukdomar"),
            new DiagnosKapitel("L00-L99Hudens och underhudens sjukdomar"),
            new DiagnosKapitel("M00-M99Sjukdomar i muskuloskeletala systemet och bindväven"),
            new DiagnosKapitel("N00-N99Sjukdomar i urin- och könsorganen"),
            new DiagnosKapitel("O00-O99Graviditet, förlossning och barnsängstid"),
            new DiagnosKapitel("P00-P96Vissa perinatala tillstånd"),
            new DiagnosKapitel("Q00-Q99Medfödda missbildningar, deformiteter och kromosomavvikelser"),
            new DiagnosKapitel(
                "R00-R99Symtom, sjukdomstecken och onormala kliniska fynd och laboratoriefynd som ej klassificeras på annan plats"),
            new DiagnosKapitel("S00-T98Skador, förgiftningar och vissa andra följder av yttre orsaker"),
            new DiagnosKapitel("U00-U99Koder för särskilda ändamål"),
            new DiagnosKapitel("V01-Y98Yttre orsaker till sjukdom och död"),
            new DiagnosKapitel("Z00-Z99Faktorer av betydelse för hälsotillståndet och för kontakter med hälso- och sjukvården"));
    }
}
