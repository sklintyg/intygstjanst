package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
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
        final var rekoStatus = rekoStatuses
                .stream()
                .filter((status) -> status.getPatientId().equals(patientId)
                        && status.getSickLeaveTimestamp().isAfter(startDate.atStartOfDay())
                        && status.getSickLeaveTimestamp().isBefore(endDate.plusDays(1).atStartOfDay())
                        && status.getCareUnitId().equals(careUnitId)
                ).max(Comparator.comparing(Reko::getRegistrationTimestamp));

        if (rekoStatus.isPresent()) {
            return RekoStatusType.fromId(rekoStatus.get().getStatus()).getName();
        }

        return RekoStatusType.REKO_1.getName();
    }
}
