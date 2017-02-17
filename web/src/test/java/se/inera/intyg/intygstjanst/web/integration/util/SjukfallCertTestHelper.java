package se.inera.intyg.intygstjanst.web.integration.util;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by eriklupander on 2017-02-17.
 */
public class SjukfallCertTestHelper {

    public static final String HSA_ID_1 = "careunit-1";
    public static final String DOCTOR_HSA_ID = "doctor-1";

    private static final String CARE_GIVER_1_ID = "caregiver-1";
    private static final String CARE_UNIT_NAME = "careunit-name-1";
    private static final String PERSONNUMMER = "191212121212";

    private static final String DOCTOR_NAME = "doctor-name-1";
    private static final String FK7263 = "fk7263";

    public List<SjukfallCertificate> intygsList() {
        List<SjukfallCertificate> certList = new ArrayList<>();
        certList.add(buildSjukfallCertificate(HSA_ID_1, CARE_GIVER_1_ID, defaultWorkCapacities(), false));
        return certList;
    }

    private SjukfallCertificate buildSjukfallCertificate(String careUnitId, String careGiverId, List<SjukfallCertificateWorkCapacity> workCapacities, boolean deleted) {
        SjukfallCertificate sc = new SjukfallCertificate(UUID.randomUUID().toString());
        sc.setCareUnitId(careUnitId);
        sc.setSjukfallCertificateWorkCapacity(workCapacities);
        sc.setCareGiverId(careGiverId);
        sc.setCareUnitName(CARE_UNIT_NAME);
        sc.setCivicRegistrationNumber(PERSONNUMMER);
        sc.setDiagnoseCode("M16");
        sc.setPatientName("Tolvan Tolvansson");
        sc.setSigningDoctorId(DOCTOR_HSA_ID);
        sc.setSigningDoctorName(DOCTOR_NAME);
        sc.setType(FK7263);
        sc.setDeleted(deleted);
        return sc;
    }

    private List<SjukfallCertificateWorkCapacity> defaultWorkCapacities() {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();

        wc.setCapacityPercentage(75);
        wc.setFromDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        wc.setToDate(LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc);

        SjukfallCertificateWorkCapacity wc2 = new SjukfallCertificateWorkCapacity();
        wc2.setCapacityPercentage(100);
        wc2.setFromDate(LocalDate.now().minusWeeks(3).format(DateTimeFormatter.ISO_DATE));
        wc2.setToDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc2);
        return workCapacities;
    }
}
