package se.inera.intyg.intygstjanst.web.integration.hsa;

import java.util.List;

/**
 * Created by eriklupander on 2016-02-02.
 */
public interface HsaService {
    List<String> getHsaIdForUnderenheter(String careUnitHsaId);

    String getHsaIdForCareGiverOfCareUnit(String careUnitHsaId);
}
