package se.inera.certificate.migration.processors;

import org.springframework.batch.item.ItemProcessor;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.model.OriginalCertificate;

public class ConvertCertificateXMLToJSONProcessor implements ItemProcessor<OriginalCertificate, Certificate> {

    public Certificate process(OriginalCertificate orgCert) throws Exception {
        
        Certificate cert = new Certificate(orgCert.getCertificateId());
        cert.setCertificateJson("Blahonga");
        
        return cert;
    }

}
