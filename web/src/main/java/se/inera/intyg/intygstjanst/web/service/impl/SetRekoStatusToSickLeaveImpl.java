package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.web.service.SetRekoStatusToSickLeave;

import java.time.LocalDate;
import java.util.List;

public class SetRekoStatusToSickLeaveImpl implements SetRekoStatusToSickLeave {

    private SjukfallCertificateDao sjukfallCertificateDao;
    private SjukfallEngineService sjukfallEngineService;
    private IntygsDataConverter intygsDataConverter;

    public SetRekoStatusToSickLeaveImpl(SjukfallCertificateDao sjukfallCertificateDao,
                                        SjukfallEngineService sjukfallEngineService,
                                        IntygsDataConverter intygsDataConverter) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.sjukfallEngineService = sjukfallEngineService;
        this.intygsDataConverter = intygsDataConverter;
    }

    //Throw error if; list is not size 1, status is not RekoStatus?
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
    }

    private LocalDate getSickLeaveTimestamp(SjukfallEnhet sickLeave) {
        if(sickLeave.getSlut().isBefore(LocalDate.now())) {
            return sickLeave.getSlut();
        } else {
            return LocalDate.now();
        }
    }
}
