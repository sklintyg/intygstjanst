package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RekoStatusDecoratorImpl implements RekoStatusDecorator {

    final RekoRepository rekoRepository;

    public RekoStatusDecoratorImpl(RekoRepository rekoRepository) {
        this.rekoRepository = rekoRepository;
    }

    @Override
    public void decorate(List<SjukfallEnhet> sickLeaves, String careUnitId) {
        final var rekoStatuses = rekoRepository.findByPatientIdInAndCareUnitId(
                sickLeaves
                        .stream()
                        .map((sickLeave) -> sickLeave.getPatient().getId())
                        .collect(Collectors.toList()), careUnitId

        );

        sickLeaves.forEach((sickLeave) -> sickLeave.setRekoStatus(
                getRekoStatus(rekoStatuses,
                        sickLeave.getPatient().getId(),
                        sickLeave.getStart(),
                        sickLeave.getSlut(),
                        sickLeave.getVardenhet().getId())
                )
        );
    }

    private String getRekoStatus(List<Reko> rekoStatuses,
                                 String patientId,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 String careUnitId) {
        return rekoStatuses
                .stream()
                .filter((status) -> status.getPatientId().equals(patientId)
                        && status.getSickLeaveTimestamp().isAfter(startDate.atStartOfDay())
                        && status.getSickLeaveTimestamp().isBefore(endDate.plusDays(1).atStartOfDay())
                        && status.getCareUnitId().equals(careUnitId)
                )
                .findFirst()
                .map(reko -> getRekoStatusName(reko.getStatus()))
                .orElseGet(RekoStatusType.REKO_1::name);
    }

    private String getRekoStatusName(String id) {
        final var status = Arrays
                .stream(RekoStatusType.values())
                .filter((type) -> type.toString().equals(id))
                .findFirst();

        if(status.isPresent()) {
            return status.get().name();
        } else {
            return RekoStatusType.REKO_1.name();
        }
    }
}
