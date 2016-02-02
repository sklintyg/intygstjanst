package se.inera.intyg.intygstjanst.web.integration.hsa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.integration.hsa.services.HsaOrganizationsService;

/**
 * Interfaces with {@link se.inera.intyg.common.integration.hsa.services.HsaOrganizationsService} from hsa-integration.
 *
 * Created by eriklupander on 2016-02-02.
 */
@Service
public class HsaServiceImpl implements HsaService {

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Override
    public List<String> getHsaIdForUnderenheter(String careUnitHsaId) {
        return hsaOrganizationsService.getHsaIdForAktivaUnderenheter(careUnitHsaId);
    }
}
