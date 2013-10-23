package se.inera.certificate.migration.listeners;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ItemListenerSupport;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.model.OriginalCertificate;

public class CertificateProcessingItemListener extends ItemListenerSupport<OriginalCertificate, Certificate> {
    
    private static Logger logger = LoggerFactory.getLogger(CertificateProcessingItemListener.class);
    
    @Override
    public void onReadError(Exception e) {
        logger.error("Exception occured when trying to read OriginalCertificate!", e);
    }
    
    @Override
    public void onProcessError(OriginalCertificate item, Exception e) {
        String errMsg = MessageFormat.format("Exception occured when processing OriginalCertificate: {0}", item);
        logger.error(errMsg, e);
    }
    
    @Override
    public void onWriteError(Exception ex, List<? extends Certificate> items) {
        String errMsg = MessageFormat.format("Exception occured when updating Certificates: {0}", items);
        logger.error(errMsg, ex);
    }
}
