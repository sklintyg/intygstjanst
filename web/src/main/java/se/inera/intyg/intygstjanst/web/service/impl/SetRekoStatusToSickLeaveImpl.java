package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.SetRekoStatusToSickLeave;

import java.time.LocalDateTime;

@Service
public class SetRekoStatusToSickLeaveImpl implements SetRekoStatusToSickLeave {
    private final RekoRepository rekoRepository;

    public SetRekoStatusToSickLeaveImpl(RekoRepository rekoRepository) {
        this.rekoRepository = rekoRepository;
    }

    @Override
    public void set(String patientId,
                    String status,
                    String careProviderId,
                    String careUnitId,
                    String unitId,
                    String staffId,
                    String staffName,
                    LocalDateTime sickLeaveTimestamp) {
        rekoRepository.save(
                getReko(
                        status,
                        patientId,
                        careProviderId,
                        careUnitId,
                        unitId,
                        staffId,
                        staffName,
                        sickLeaveTimestamp
                )
        );
    }

    private Reko getReko(String status,
                         String patientId,
                         String careProviderId,
                         String careUnitId,
                         String unitId,
                         String staffId,
                         String staffName,
                         LocalDateTime sickLeaveTimestamp
    ) {
        final var reko = new Reko();
        reko.setPatientId(patientId);
        reko.setStatus(status);
        reko.setSickLeaveTimestamp(sickLeaveTimestamp);
        reko.setCareProviderId(careProviderId);
        reko.setCareUnitId(careUnitId);
        reko.setUnitId(unitId);
        reko.setRegistrationTimestamp(LocalDateTime.now());
        reko.setStaffId(staffId);
        reko.setStaffName(staffName);

        return reko;
    }
}
