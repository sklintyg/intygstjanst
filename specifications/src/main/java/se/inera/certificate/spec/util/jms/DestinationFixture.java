package se.inera.certificate.spec.util.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;

/**
 * Generic Fixture for retrieving JMS queues.
 */
public abstract class DestinationFixture {

    protected static Map<String, Destination> destinations = new HashMap<String, Destination>();
    
    /**
     * Get destination.
     * 
     * @return the connection
     */
    public static Destination getDestination(String destination) {
        if (destinations.containsKey(destination)) {
            return destinations.get(destination);
        } else {
            throw new IllegalStateException("Destination not configured");
        }
    }

    /**
     * Register destination.
     */
    public void registerDestinationAs(String name, String lookup) throws JMSException {
    }

    /**
     * Register Queue.
     */
    public void registerQueueAs(String name, String lookup) throws JMSException {
        registerDestinationAs(name, lookup);
    }

    /**
     * Register Topic.
     */
    public void registerTopicAs(String name, String lookup) throws JMSException {
        registerDestinationAs(name, lookup);
    }

}
