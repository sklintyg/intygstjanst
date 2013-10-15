package se.inera.certificate.migration.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Program for generating test data and loading it into a database. 
 * 
 * @author nikpet
 *
 */
public class CertificateDbLoader {
    
    private static final int NBR_OF_CERTS = 100;
    
    public static Logger logger = LoggerFactory.getLogger(CertificateDbLoader.class);
    
    public static void main(String[] args) throws Exception {
        
        logger.info("Initializing Spring context.");
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/test-loader-context.xml");
        
        logger.info("Spring context initialized.");

        CertificateDataInitialiser dataInitialiser = (CertificateDataInitialiser) applicationContext.getBean("certificateDataInitialiser");
        
        dataInitialiser.generateAndLoadCerts(NBR_OF_CERTS);
        
        logger.info("Loading done!");
    }
    
}
