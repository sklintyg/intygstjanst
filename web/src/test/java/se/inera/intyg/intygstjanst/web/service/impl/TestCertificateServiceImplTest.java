/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.web.service.EraseTestCertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;

@RunWith(MockitoJUnitRunner.class)
public class TestCertificateServiceImplTest {

    @Mock
    private CertificateDao certificateDao;

    @Mock
    private RelationDao relationDao;

    @Mock
    private EraseTestCertificateService eraseTestCertificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private TestCertificateServiceImpl testCertificateService;

    private final static LocalDateTime FROM = LocalDateTime.of(2020, 04, 01, 0, 0);
    private final static LocalDateTime TO = LocalDateTime.of(2020, 04, 30, 0, 0);


    final Certificate testCertificateRoot = mock(Certificate.class);
    private final static String TEST_CERTIFICATE_ROOT_ID = "TEST_CERTIFICATE_ROOT_ID";
    private final static String TEST_CERTIFICATE_ROOT_UNIT_ID = "TEST_CERTIFICATE_ROOT_UNIT_ID";

    final Certificate testCertificateBranch = mock(Certificate.class);
    private final static String TEST_CERTIFICATE_BRANCH_ID = "TEST_CERTIFICATE_BRANCH_ID";
    private final static String TEST_CERTIFICATE_BRANCH_UNIT_ID = "TEST_CERTIFICATE_BRANCH_UNIT_ID";

    final Certificate testCertificateLeaf = mock(Certificate.class);
    private final static String TEST_CERTIFICATE_LEAF_ID = "TEST_CERTIFICATE_LEAF_ID";
    private final static String TEST_CERTIFICATE_LEAF_UNIT_ID = "TEST_CERTIFICATE_LEAF_UNIT_ID";

    final Certificate testCertificateSingle = mock(Certificate.class);
    private final static String TEST_CERTIFICATE_SINGLE_ID = "TEST_CERTIFICATE_SINGLE_ID";
    private final static String TEST_CERTIFICATE_SINGLE_UNIT_ID = "TEST_CERTIFICATE_SINGLE_UNIT_ID";

    @Before
    public void setupTestCertificates() throws Exception {
        doReturn(TEST_CERTIFICATE_ROOT_ID).when(testCertificateRoot).getId();
        doReturn(TEST_CERTIFICATE_ROOT_UNIT_ID).when(testCertificateRoot).getCareUnitId();
        doReturn(testCertificateRoot).when(certificateDao).getCertificate(null, TEST_CERTIFICATE_ROOT_ID);

        doReturn(TEST_CERTIFICATE_BRANCH_ID).when(testCertificateBranch).getId();
        doReturn(TEST_CERTIFICATE_BRANCH_UNIT_ID).when(testCertificateBranch).getCareUnitId();
        doReturn(testCertificateBranch).when(certificateDao).getCertificate(null, TEST_CERTIFICATE_BRANCH_ID);

        doReturn(TEST_CERTIFICATE_LEAF_ID).when(testCertificateLeaf).getId();
        doReturn(TEST_CERTIFICATE_LEAF_UNIT_ID).when(testCertificateLeaf).getCareUnitId();
        doReturn(testCertificateLeaf).when(certificateDao).getCertificate(null, TEST_CERTIFICATE_LEAF_ID);

        doReturn(TEST_CERTIFICATE_SINGLE_ID).when(testCertificateSingle).getId();
        doReturn(TEST_CERTIFICATE_SINGLE_UNIT_ID).when(testCertificateSingle).getCareUnitId();
        doReturn(testCertificateSingle).when(certificateDao).getCertificate(null, TEST_CERTIFICATE_SINGLE_ID);
    }

    @Test
    public void testEraseTestCertificateSingleCertificate() throws Exception {
        final var certificateList = new ArrayList<Certificate>(1);
        certificateList.add(testCertificateSingle);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(1, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(1)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseTestCertificateSingleCertificateFailed() throws Exception {
        final var certificateList = new ArrayList<Certificate>(1);
        certificateList.add(testCertificateSingle);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        doThrow(new RuntimeException()).when(eraseTestCertificateService).eraseTestCertificates(any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(0, actualEraseResult.getErasedCount());
        assertEquals(1, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(0)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseTestCertificateSingleCertificateMissingCareUnit() throws Exception {
        final var certificateList = new ArrayList<Certificate>(1);
        certificateList.add(testCertificateSingle);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        doThrow(new RuntimeException()).when(certificateDao).getCertificate(null, TEST_CERTIFICATE_SINGLE_ID);

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(1, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(1)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphWhenAllMatchFindQuery() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(3);
        certificateList.add(testCertificateRoot);
        certificateList.add(testCertificateBranch);
        certificateList.add(testCertificateLeaf);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphWhenRootMatchFindQuery() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(1);
        certificateList.add(testCertificateRoot);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphWhenBranchMatchFindQuery() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(1);
        certificateList.add(testCertificateBranch);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3, actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphWhenLeafMatchFindQuery() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(1);
        certificateList.add(testCertificateLeaf);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3 , actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(1)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphAndSingleCertificate() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(2);
        certificateList.add(testCertificateLeaf);
        certificateList.add(testCertificateSingle);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(4 , actualEraseResult.getErasedCount());
        assertEquals(0, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(2)).eraseTestCertificates(any());
        verify(monitoringLogService, times(4)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphSucessAndSingleCertificateFailed() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(2);
        certificateList.add(testCertificateLeaf);
        certificateList.add(testCertificateSingle);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());
        doThrow(new RuntimeException()).when(eraseTestCertificateService).eraseTestCertificates(argThat(new ArgumentMatcher<List<String>>() {
            @Override
            public boolean matches(List<String> argument) {
                return argument.size() == 1;
            }
        }));

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(3 , actualEraseResult.getErasedCount());
        assertEquals(1, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(2)).eraseTestCertificates(any());
        verify(monitoringLogService, times(3)).logTestCertificateErased(any(), any());
    }

    @Test
    public void testEraseFullGraphFailedAndSingleCertificateSuccess() throws Exception {
        setUpTestDataWithFullRelationGraph();

        final var certificateList = new ArrayList<Certificate>(2);
        certificateList.add(testCertificateLeaf);
        certificateList.add(testCertificateSingle);

        doReturn(certificateList).when(certificateDao).findTestCertificates(any(), any());
        doThrow(new RuntimeException()).when(eraseTestCertificateService).eraseTestCertificates(argThat(new ArgumentMatcher<List<String>>() {
            @Override
            public boolean matches(List<String> argument) {
                return argument.size() == 3;
            }
        }));

        final var actualEraseResult = testCertificateService.eraseTestCertificates(FROM, TO);

        assertEquals(1 , actualEraseResult.getErasedCount());
        assertEquals(3, actualEraseResult.getFailedCount());
        verify(eraseTestCertificateService, times(2)).eraseTestCertificates(any());
        verify(monitoringLogService, times(1)).logTestCertificateErased(any(), any());
    }

    private void setUpTestDataWithFullRelationGraph() {
        final var relationRootToBranch = mock(Relation.class);
        doReturn(TEST_CERTIFICATE_ROOT_ID).when(relationRootToBranch).getFromIntygsId();
        doReturn(TEST_CERTIFICATE_BRANCH_ID).when(relationRootToBranch).getToIntygsId();

        final var relationBranchToLeaf = mock(Relation.class);
        doReturn(TEST_CERTIFICATE_BRANCH_ID).when(relationBranchToLeaf).getFromIntygsId();
        doReturn(TEST_CERTIFICATE_LEAF_ID).when(relationBranchToLeaf).getToIntygsId();

        final var relationList = new ArrayList<Relation>(2);
        relationList.add(relationRootToBranch);
        relationList.add(relationBranchToLeaf);

        doReturn(relationList).when(relationDao).getGraph(TEST_CERTIFICATE_ROOT_ID);
        doReturn(relationList).when(relationDao).getGraph(TEST_CERTIFICATE_BRANCH_ID);
        doReturn(relationList).when(relationDao).getGraph(TEST_CERTIFICATE_LEAF_ID);
    }
}
