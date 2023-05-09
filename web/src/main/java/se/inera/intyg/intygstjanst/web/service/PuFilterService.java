package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.infra.sjukfall.dto.IntygData;

import java.util.List;

public interface PuFilterService {

    void enrichWithPatientNameAndFilter(List<IntygData> sickLeaves, boolean filterProtectedPerson);
}
