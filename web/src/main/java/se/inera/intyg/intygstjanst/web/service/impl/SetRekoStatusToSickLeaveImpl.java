package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.web.service.SetRekoStatusToSickLeave;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SetRekoStatusToSickLeaveImpl implements SetRekoStatusToSickLeave {

    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final SjukfallEngineService sjukfallEngineService;
    private final IntygsDataConverter intygsDataConverter;
    private final RekoRepository rekoRepository;

    public SetRekoStatusToSickLeaveImpl(SjukfallCertificateDao sjukfallCertificateDao,
                                        SjukfallEngineService sjukfallEngineService,
                                        IntygsDataConverter intygsDataConverter) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.sjukfallEngineService = sjukfallEngineService;
        this.intygsDataConverter = intygsDataConverter;
    }

    //Throw error if: list is not size 1; status is not RekoStatus?
    @Override
    public void set(String patientId,
                    String status,
                    String careProviderId,
                    String careUnitId,
                    String unitId,
                    int maxCertificateGap,
                    int maxDaysSinceSickLeaveCompleted) {

        final var sjukfallCertificate = sjukfallCertificateDao.findAllSjukfallCertificate(
                careProviderId,
                List.of(careUnitId),
                List.of(patientId)
        );

        final var intygDataList = intygsDataConverter.convert(sjukfallCertificate);

        final var sickLeaves = sjukfallEngineService.beraknaSjukfallForEnhet(
                intygDataList,
                new IntygParametrar(
                        maxCertificateGap,
                        maxDaysSinceSickLeaveCompleted,
                        LocalDate.now()
                )
        );

        final var sickLeaveTimestamp = getSickLeaveTimestamp(sickLeaves.get(0));

        rekoRepository.add(new RekoStatusDTO(patientId, RekoStatusType.valueOf(status), LocalDateTime.now(), sickLeaveTimestamp, careProviderId, careUnitId, unitId));
    }

    private LocalDateTime getSickLeaveTimestamp(SjukfallEnhet sickLeave) {
        if(sickLeave.getSlut().isBefore(LocalDate.now())) {
            return sickLeave.getSlut().atStartOfDay();
        } else {
            return LocalDateTime.now();
        }
    }
}
