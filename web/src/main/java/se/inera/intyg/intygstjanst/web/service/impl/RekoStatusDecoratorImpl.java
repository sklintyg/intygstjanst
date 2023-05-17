package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class RekoStatusDecoratorImpl implements RekoStatusDecorator {

    final RekoRepository rekoRepository;

    public RekoStatusDecoratorImpl(RekoRepository rekoRepository) {
        this.rekoRepository = rekoRepository;
    }

    @Override
    public void decorate(List<SjukfallEnhet> sickLeaves) {
        final var rekoStatuses = rekoRepository.getRekoStatusesForSickLeaves(
                sickLeaves
                        .stream()
                        .map((sickLeave) -> sickLeave.getPatient().getId())
                        .collect(Collectors.toList())

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

    private String getRekoStatus(List<RekoStatusDTO> rekoStatuses,
                                 String patientId,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 String careUnitId) {
        final var rekoStatus = rekoStatuses
                .stream()
                .filter((status) -> status.getPatientId().equals(patientId)
                        && status.getSickLeaveTimestamp().isAfter(startDate.atStartOfDay())
                        && status.getSickLeaveTimestamp().isBefore(endDate.plusDays(1).atStartOfDay())
                        && status.getCareUnitId().equals(careUnitId)
                )
                .findFirst();

        if (rekoStatus.isPresent()) {
            return rekoStatus.get().getStatus().name();
        }

        return RekoStatusType.REKO_1.name();
    }
}
