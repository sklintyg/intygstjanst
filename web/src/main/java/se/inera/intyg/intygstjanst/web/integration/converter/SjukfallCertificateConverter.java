package se.inera.intyg.intygstjanst.web.integration.converter;

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts JPA entity model {@link SjukfallCertificate} to the
 * infra/sjukfall/engine {@link IntygData} format.
 *
 * Created by eriklupander on 2017-02-17.
 */
@Service
public class SjukfallCertificateConverter {

    public List<IntygData> convert(List<SjukfallCertificate> list) {
        return list.stream().map(sc -> {
            IntygData intyg = new IntygData();
            intyg.setIntygId(intyg.getIntygId());
            intyg.setEnkeltIntyg(false);
            intyg.setFormagor(buildFormaga(sc.getSjukfallCertificateWorkCapacity()));
            intyg.setDiagnosKod(new DiagnosKod(sc.getDiagnoseCode()));
            intyg.setLakareId(sc.getSigningDoctorId());
            intyg.setLakareNamn(sc.getSigningDoctorName());
            intyg.setSigneringsTidpunkt(sc.getSigningDateTime());
            intyg.setPatientId(sc.getCivicRegistrationNumber());
            intyg.setPatientNamn(sc.getPatientName());
            intyg.setVardenhetId(sc.getCareUnitId());
            intyg.setVardenhetNamn(sc.getCareUnitName());
            return intyg;
        }).collect(Collectors.toList());
    }

    private List<Formaga> buildFormaga(List<SjukfallCertificateWorkCapacity> workCapacities) {

        return workCapacities.stream()
                .map(this::buildFormaga)
                .collect(Collectors.toList());
    }

    private Formaga buildFormaga(SjukfallCertificateWorkCapacity wc) {
        return new Formaga(LocalDate.parse(wc.getFromDate()), LocalDate.parse(wc.getToDate()), wc.getCapacityPercentage());
    }

}
