package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.web.service.SetRekoStatusToSickLeave;

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
                                        IntygsDataConverter intygsDataConverter, RekoRepository rekoRepository) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.sjukfallEngineService = sjukfallEngineService;
        this.intygsDataConverter = intygsDataConverter;
        this.rekoRepository = rekoRepository;
    }

    //Throw error if: list is not size 1; status is not RekoStatus?
    @Override
    public void set(String patientId,
                    String status,
                    String careProviderId,
                    String careUnitId,
                    String unitId,
                    String staffId,
                    String staffName,
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


        rekoRepository.save(getReko(status, patientId, sickLeaveTimestamp, careProviderId, careUnitId, unitId, staffId, staffName));
    }

    private Reko getReko(String status,
                         String patientId,
                         LocalDateTime sickLeaveTimeStamp,
                         String careProviderId,
                         String careUnitId,
                         String unitId,
                         String staffId,
                         String staffName
    ) {
        final var reko = new Reko();
        reko.setPatientId(patientId);
        reko.setStatus(status);
        reko.setSickLeaveTimestamp(sickLeaveTimeStamp);
        reko.setCareProviderId(careProviderId);
        reko.setCareUnitId(careUnitId);
        reko.setUnitId(unitId);
        reko.setRegistrationTimestamp(LocalDateTime.now());
        reko.setStaffId(staffId);
        reko.setStaffName(staffName);

        return reko;
    }

    private LocalDateTime getSickLeaveTimestamp(SjukfallEnhet sickLeave) {
        if(sickLeave.getSlut().isBefore(LocalDate.now())) {
            return sickLeave.getSlut().atStartOfDay();
        } else {
            return LocalDateTime.now();
        }
    }
}
