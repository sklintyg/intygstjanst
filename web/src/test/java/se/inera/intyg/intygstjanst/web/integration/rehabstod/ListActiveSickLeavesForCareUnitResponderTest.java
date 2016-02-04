package se.inera.intyg.intygstjanst.web.integration.rehabstod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;

/**
 * Created by eriklupander on 2016-02-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListActiveSickLeavesForCareUnitResponderTest {

    private static final String HSA_ID = "enhet-1";
    private static final String CARE_GIVER_ID = "vardgivare-1";
    @Mock
    private HsaService hsaService;

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @InjectMocks
    private ListActiveSickLeavesForCareUnitResponderImpl testee;
    private HsaId hsaId = new HsaId();

    @Before
    public void init() {
        hsaId.setExtension(HSA_ID);
    }

    @Test
    public void testWithNoCareUnitId() {
        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.ERROR, responseType.getResultCode());
    }

    @Test
    public void testWithKnownCareGiver() {
        when(hsaService.getHsaIdForCareGiverOfCareUnit(HSA_ID)).thenReturn(CARE_GIVER_ID);
        when(hsaService.getHsaIdForUnderenheter(HSA_ID)).thenReturn(new ArrayList<>());

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResultCode());
    }

    @Test
    public void testWithUnknownCareGiver() {
        when(hsaService.getHsaIdForCareGiverOfCareUnit(HSA_ID)).thenReturn(null);
        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.ERROR, responseType.getResultCode());
    }
}
