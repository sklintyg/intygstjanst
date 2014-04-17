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
    
    public static Logger logger = LoggerFactory.getLogger(CertificateDbLoader.class);
    
    public static void main(String[] args) throws Exception {
        
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("The number of certs to load must be supplied!");
        }
        
        logger.info("Initializing Spring context.");
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/test-loader-context.xml");
        
        logger.info("Spring context initialized.");

        CertificateDataInitialiser dataInitialiser = (CertificateDataInitialiser) applicationContext.getBean("certificateDataInitialiser");
        
        int nbrOfCerts = Integer.parseInt(args[0]);
        dataInitialiser.generateAndLoadCerts(nbrOfCerts, true);
        
        logger.info("Loading done!");
    }
    
}
