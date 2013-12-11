package se.inera.certificate.migration.testutils.jms;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util for inspecting a Queue using JMX.
 * 
 * @author nikpet
 *
 */
public class TestQueueInspector {
    
    private static Logger log = LoggerFactory.getLogger(TestQueueInspector.class);
    
    private MBeanServerConnection mBeanConn;
    
    public TestQueueInspector() {

    }

    public Long getQueueSize(String queueName) {
        
        Long queueSize = null;
        
        try {

            ObjectName objectNameRequest = new ObjectName(
                    "org.apache.activemq:type=Broker,BrokerName=local*,destinationType=Queue,DestinationName=" + queueName);

            queueSize = (Long) mBeanConn.getAttribute(objectNameRequest, "QueueSize");

            return queueSize;
            
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return queueSize;
    }

    public MBeanServerConnection getMbeanConn() {
        return mBeanConn;
    }

    public void setMBeanConn(MBeanServerConnection mbeanConn) {
        this.mBeanConn = mbeanConn;
    }

}
