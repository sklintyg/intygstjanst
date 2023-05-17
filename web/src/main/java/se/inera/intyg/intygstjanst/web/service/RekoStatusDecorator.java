package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;

import java.util.List;

public interface RekoStatusDecorator {
    void decorate(List<SjukfallEnhet> sickLeaves);
}
