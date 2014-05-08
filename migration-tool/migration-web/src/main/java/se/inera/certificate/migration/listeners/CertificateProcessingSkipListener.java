package se.inera.certificate.migration.listeners;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListenerSupport;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.model.OriginalCertificate;

/**
 * Step listener for logging skipped Certificates.
 * 
 * 
 * @author nikpet
 *
 */
public class CertificateProcessingSkipListener extends SkipListenerSupport<OriginalCertificate, Certificate> {

    private static Logger logger = LoggerFactory.getLogger(CertificateProcessingSkipListener.class);
    
    @Override
    public void onSkipInRead(Throwable t) {
        logger.error("Skip executed when trying to read OriginalCertificate!", t);
    }
    
    @Override
    public void onSkipInProcess(OriginalCertificate item, Throwable t) {
        String errMsg = MessageFormat.format("Skip executed when processing of OriginalCertificate failed: {0}", item);
        logger.error(errMsg, t);
    }
    
    @Override
    public void onSkipInWrite(Certificate item, Throwable t) {
        String errMsg = MessageFormat.format("Skip executed when updating Certificate: {0}", item);
        logger.error(errMsg, t);
    }
    
}
